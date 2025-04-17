package com.byteforge.quiz.controller;

import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.quiz.dto.QuizRequestDto;
import com.byteforge.quiz.dto.QuizResultDto;
import com.byteforge.quiz.dto.QuizSubmissionDto;
import com.byteforge.quiz.model.QuizAttempt;
import com.byteforge.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;
    private final UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<QuizService.StartQuizResponse> startQuiz(@Valid @RequestBody QuizRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));

        QuizService.StartQuizResponse response = quizService.startQuiz(requestDto, user.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultDto> submitQuiz(@Valid @RequestBody QuizSubmissionDto submissionDto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));

        QuizResultDto result = quizService.submitQuiz(submissionDto, user.getId());
        return ResponseEntity.ok(result);
    }
}
