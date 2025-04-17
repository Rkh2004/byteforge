package com.byteforge.quiz.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "questions")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for this question

    // Many questions belong to one quiz attempt. This is the owning side of the relationship.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false) // Foreign key linking to the QuizAttempt table
    private QuizAttempt quizAttempt;

    @Column(columnDefinition = "TEXT", nullable = false) // Use TEXT for potentially long question text
    private String questionText; // The actual text of the question

    // Store the multiple-choice options as a JSON string.
    // Example: {"A": "Option 1 Text", "B": "Option 2 Text", "C": "Option 3 Text", "D": "Option 4 Text"}
    @Column(columnDefinition = "TEXT", nullable = false)
    private String options;

    @Column(length = 1, nullable = false) // Store just the key ('A', 'B', 'C', or 'D')
    private String correctAnswer; // The key corresponding to the correct option

    // Optional: Store the parameters under which this question was generated for analysis
    private String requestedDifficulty;
    private String requestedType;
}