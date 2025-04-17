package com.byteforge.exception;// quiz/exception/QuizGenerationException.java

// Custom exception specific to errors occurring during the AI quiz generation process.
public class QuizGenerationException extends RuntimeException {
    public QuizGenerationException(String message) {
        super(message);
    }

    public QuizGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}