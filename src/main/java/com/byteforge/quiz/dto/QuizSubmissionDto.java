package com.byteforge.quiz.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record QuizSubmissionDto(
        @NotNull(message = "quiz attempt Id required")
        Long quizAttemptId,

        @NotEmpty(message = "Answers cannot be empty.")
        Map<Long, String> answers
) {}
