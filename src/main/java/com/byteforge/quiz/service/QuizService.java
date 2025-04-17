package com.byteforge.quiz.service;

import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.quiz.dto.QuizQuestionDto;
import com.byteforge.quiz.dto.QuizRequestDto;
import com.byteforge.quiz.dto.QuizResultDto;
import com.byteforge.quiz.dto.QuizSubmissionDto;
import com.byteforge.quiz.model.Question;
import com.byteforge.quiz.model.QuizAttempt;
import com.byteforge.quiz.repository.QuizAttemptRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private GeminiQuizGenerator geminiQuizGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;



    public record StartQuizResponse(Long quizAttemptId, List<QuizQuestionDto> questions) {}

    @Transactional
    public StartQuizResponse startQuiz(QuizRequestDto requestDto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Generate questions using AI
        List<GeminiQuizGenerator.AiQuestion> aiQuestions = geminiQuizGenerator.generateMcqQuiz(
                requestDto.topics(),
                requestDto.difficulty().name(),
                requestDto.questionType().name(),
                requestDto.numberOfQuestions()
        );

        // Create the QuizAttempt entity
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setRequestedQuestionCount(requestDto.numberOfQuestions());
        attempt.setRequestedDifficulty(requestDto.difficulty());
        attempt.setRequestedQuestionType(requestDto.questionType());
        attempt.setTotalQuestions(aiQuestions.size());

        try {
            attempt.setSelectedTopicsJson(objectMapper.writeValueAsString(requestDto.topics()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing request data", e);
        }

        // Create question entities for each ai generated question
        List<Question> questionEntities = new ArrayList<>();
        for(GeminiQuizGenerator.AiQuestion aiQuestion : aiQuestions){
            Question questionEntity = new Question();
            questionEntity.setQuestionText(aiQuestion.question());
            questionEntity.setCorrectAnswer(aiQuestion.correctAnswer());
            questionEntity.setRequestedDifficulty(requestDto.difficulty().name());
            questionEntity.setRequestedType(requestDto.questionType().name());
            try{
                questionEntity.setOptions(objectMapper.writeValueAsString(aiQuestion.options()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error processing request data",e);
            }
            attempt.addQuestion(questionEntity);
        }

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Generate quiz questions for frontend
        List<QuizQuestionDto> questionsForFrontend = savedAttempt.getQuestions().stream()
                .map(q -> {
                    try {
                        // Deserialize the options JSON back into a Map for the DTO
                        Map<String, String> optionsMap = objectMapper.readValue(q.getOptions(), new TypeReference<Map<String, String>>() {});
                        return new QuizQuestionDto(q.getId(), q.getQuestionText(), optionsMap);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(dto -> dto != null) // Filter out any questions that failed deserialization
                .toList();

        return new StartQuizResponse(savedAttempt.getId(), questionsForFrontend);
    }

    @Transactional
    public QuizResultDto submitQuiz(QuizSubmissionDto quizSubmissionDto, Long userId){

        // Find the quiz attempt by id
        QuizAttempt attempt = quizAttemptRepository.findById(quizSubmissionDto.quizAttemptId())
                .orElseThrow(() -> new IllegalArgumentException("Exception finding quiz attempt"));

        // Validate if the user sending the request owns the quiz attempt
        if(!attempt.getUser().getId().equals(userId)){
            throw new SecurityException("User does not have permission to submit this quiz attempt.");
        }

        // check if the quiz is already submitted
        if (attempt.getEndTime() != null) {
            throw new IllegalStateException("This quiz has already been submitted.");
        }

        // Compare and match the answers and give score
        int score = 0;
        Map<Long, String> correctAnswers = new HashMap<>();
        Map<Long, String> userAnswers = quizSubmissionDto.answers();

        for(Question question : attempt.getQuestions()){
            correctAnswers.put(question.getId(), question.getCorrectAnswer());

            String userAnswer = userAnswers.get(question.getId());

            if(userAnswer!=null && userAnswer.equalsIgnoreCase(question.getCorrectAnswer())){
                score++;
            }
        }

        attempt.setEndTime(LocalDateTime.now());
        attempt.setScore(score);
        try{
            attempt.setUserAnswers(objectMapper.writeValueAsString(userAnswers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Exception processing user answers");
        }

        quizAttemptRepository.save(attempt);

        return new QuizResultDto(attempt.getId(),attempt.getScore(),attempt.getTotalQuestions(),userAnswers, correctAnswers);
    }

}
