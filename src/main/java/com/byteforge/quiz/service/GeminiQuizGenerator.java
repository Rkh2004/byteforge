package com.byteforge.quiz.service;

import com.byteforge.exception.QuizGenerationException;
import com.byteforge.quiz.model.Difficulty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiQuizGenerator {

    private final ChatClient.Builder geminiChatClient;
    private final ObjectMapper objectMapper;

    public record AiQuizResponse(List<AiQuestion> questions){}
    public record AiQuestion(String question, Map<String, String> options, String correctAnswer){}

    public List<AiQuestion> generateMcqQuiz(List<String> topicNames, String difficulty, String questionType, int numberOfQuestions){
        String promptString = buildPrompt(topicNames, difficulty, questionType, numberOfQuestions);
        Prompt aiPrompt = new Prompt(promptString);

        try {
            ChatClient chatClient = geminiChatClient.build();
            // Step 2: Call the Gemini API via Spring AI ChatClient
            ChatResponse response = chatClient.prompt(aiPrompt).call().chatResponse();
            String jsonResponse = response.getResult().getOutput().getText();

            // Step 3: Clean and parse the JSON response
            jsonResponse = cleanJsonResponse(jsonResponse); // Remove potential markdown formatting
            AiQuizResponse quizResponse = objectMapper.readValue(jsonResponse, AiQuizResponse.class);

            // Step 4: Validate the parsed response
            if (quizResponse == null || quizResponse.questions() == null || quizResponse.questions().isEmpty()) {
                throw new QuizGenerationException("AI returned empty or invalid quiz data.");
            }
            // TODO: Add more validation if needed (e.g., check options count, valid correctAnswer key)

            return quizResponse.questions();

        } catch (JsonProcessingException e) {
            throw new QuizGenerationException("Failed to parse quiz data from AI.", e);
        } catch (Exception e) {
            // Catching broader exceptions from the AI call itself or other unexpected issues
            throw new QuizGenerationException("An unexpected error occurred during AI communication or processing.", e);
        }
    }

    private String buildPrompt(List<String> topicNames, String difficulty, String questionType, int numberOfQuestions) {
        String topicsString = String.join(", ", topicNames);
        String typeDescription = switch (questionType.toUpperCase()) {
            case "CODING" -> "coding implementation concepts, syntax, or problem-solving";
            case "THEORY" -> "theoretical concepts, definitions, and principles";
            case "BOTH" -> "a mix of theoretical concepts and coding-related questions";
            default -> "general concepts"; // Fallback
        };

        // Using Java Text Block for better readability of the multi-line prompt
        return """
               Generate a multiple-choice quiz with exactly %d questions covering the following topics: %s.
               The difficulty level should be %s.
               Focus the questions on %s.

               RULES:
               1. Each question MUST have exactly 4 options, labeled A, B, C, D.
               2. Each question MUST have only one single correct answer.
               3. Indicate the correct answer using the key ("A", "B", "C", or "D") for each question.

               RESPONSE FORMAT:
               Respond ONLY with a valid JSON object. Do NOT include any text before or after the JSON object.
               The JSON object must contain a single key "questions".
               The value of "questions" must be a JSON array.
               Each element in the array represents a single question and MUST be a JSON object with the following exact keys and value types:
                 - "question": (String) The text of the question.
                 - "options": (Object) A JSON object containing exactly four key-value pairs, where keys are "A", "B", "C", "D" and values are the option strings.
                 - "correctAnswer": (String) The key ("A", "B", "C", or "D") of the correct option.

               EXAMPLE of the required JSON output format:
               {
                 "questions": [
                   {
                     "question": "What is the capital of France?",
                     "options": {
                       "A": "Berlin",
                       "B": "Madrid",
                       "C": "Paris",
                       "D": "Rome"
                     },
                     "correctAnswer": "C"
                   },
                   {
                     // ... more question objects following the same structure ...
                   }
                 ]
               }

               Ensure the entire response is just this JSON structure. No introductory text, no explanations, no markdown code blocks (```json ... ```).
               """.formatted(numberOfQuestions, topicsString, difficulty, typeDescription);
    }

    private String cleanJsonResponse(String rawResponse) {
        String cleaned = rawResponse.trim();
        // Find the first '{' and the last '}' to extract the core JSON object
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }
        // Fallback: Basic cleaning for markdown fences if the brace extraction didn't work well
        else {
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
        }
        return cleaned.trim();
    }

}
