package com.byteforge.quiz.dto;

import java.util.Map;

public record QuizQuestionDto(
    Long questionId,
    String questionText,
    Map<String, String> options
) {}
