package com.byteforge.quiz.dto;

import java.util.Map;

public record QuizResultDto(
        Long quizAttemptId,
        int score,
        int totalQuestions,
        Map<Long, String> userAnswers,
        Map<Long, String> correctAnswers
) {}