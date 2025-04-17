package com.byteforge.quiz.model;

import com.byteforge.auth.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for this specific quiz attempt

    @ManyToOne(fetch = FetchType.LAZY) // Many attempts can belong to one user
    @JoinColumn(name = "user_id", nullable = false) // Foreign key linking to the User table
    private User user; // The user who took this quiz

    @Column(nullable = false)
    private LocalDateTime startTime; // Timestamp when the quiz was started

    private LocalDateTime endTime; // Timestamp when the quiz was submitted (null if in progress)

    // Store the names of the topics selected by the user for this attempt.
    // Since topics aren't stored in the DB, we save the names provided by the frontend.
    // Using JSON format is flexible. Example: ["Java", "Spring Boot"]
    @Column(columnDefinition = "TEXT")
    private String selectedTopicsJson;

    @Column(nullable = false)
    private int requestedQuestionCount; // How many questions the user initially requested

    @Enumerated(EnumType.STRING) // Store enum names (EASY, MEDIUM, HARD) as strings in the DB
    @Column(nullable = false)
    private Difficulty requestedDifficulty; // The difficulty level selected by the user

    @Enumerated(EnumType.STRING) // Store enum names (THEORY, CODING, BOTH) as strings
    @Column(nullable = false)
    private QuestionType requestedQuestionType; // The type of questions requested

    // One attempt has many questions.
    // CascadeType.ALL: If an attempt is saved/deleted, associated questions are also saved/deleted.
    // orphanRemoval=true: If a question is removed from this list, it's deleted from the DB.
    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>(); // The list of questions generated for this attempt

    // Store the user's submitted answers.
    // Using JSON format: {"questionId1": "selectedOptionKeyA", "questionId2": "selectedOptionKeyC", ...}
    @Column(columnDefinition = "TEXT")
    private String userAnswers;

    private Integer score; // The calculated score (e.g., number of correct answers), null until submitted
    private Integer totalQuestions; // The actual number of questions generated (might differ slightly from requested)

    // --- Enums defined within the entity or separately ---


    // Helper method to add a question and maintain the bidirectional relationship
    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setQuizAttempt(this);
    }
}