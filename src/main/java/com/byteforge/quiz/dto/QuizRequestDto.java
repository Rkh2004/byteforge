package com.byteforge.quiz.dto;
import com.byteforge.quiz.model.Difficulty;
import com.byteforge.quiz.model.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record QuizRequestDto(
        @NotEmpty(message = "the topics list cannot be empty")
        List<String> topics,

        @NotNull(message = "difficulty must be selected")
        Difficulty difficulty,

        @NotNull(message = "Question type must be selected.")
        QuestionType questionType,

        @Min(value = 1, message = "Number of questions must be at least 1.")
        int numberOfQuestions
) {}
