package com.byteforge.bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long userId; // Link to your user identifier (e.g., username or UUID from SecurityContext)

    @Column(nullable = false, updatable = false)
    private String topicId; // e.g., "inheritance", "polymorphism", "variables-datatypes"

    @Lob // Use Lob for potentially long message content
    @Column(nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SenderType senderType; // USER or AI

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }
}
