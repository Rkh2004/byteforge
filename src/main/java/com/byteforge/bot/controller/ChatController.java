package com.byteforge.bot.controller;

import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.bot.dto.ChatRequest;
import com.byteforge.bot.dto.ChatResponse;
import com.byteforge.bot.model.ChatMessage;
import com.byteforge.bot.repository.ChatMessageRepository;
import com.byteforge.bot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
public class ChatController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @PostMapping("/{topic}")
    public ResponseEntity<ChatResponse> handleChatMessage(
            @PathVariable String topic,
            @Valid @RequestBody ChatRequest chatRequest,
            @AuthenticationPrincipal UserDetails userDetails
            ){

        User user = getAuthenticatedUser(userDetails);

        ChatResponse chatResponse = chatService.processMessage(user, topic, chatRequest);

        return ResponseEntity.ok(chatResponse);
    }

    // TODO : create a mapper class to map ChatMessage to appropriate history response
    @GetMapping("/{topic}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String topic, @AuthenticationPrincipal UserDetails userDetails){
        User user = getAuthenticatedUser(userDetails);

        List<ChatMessage> messages = chatMessageRepository.findByUserIdAndTopicIdOrderByTimestampAsc(user.getId(), topic);
        return ResponseEntity.ok(messages);
    }

    @Transactional
    @DeleteMapping("/{topic}")
    public ResponseEntity<?> clearHistory(@PathVariable String topic, @AuthenticationPrincipal UserDetails userDetails){
        User user = getAuthenticatedUser(userDetails);
        chatMessageRepository.deleteAllByUserIdAndTopicId(user.getId(), topic);
        return ResponseEntity.ok("History cleared successfully");
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

}
