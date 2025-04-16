package com.byteforge.bot.controller;

import com.byteforge.auth.model.User;
import com.byteforge.auth.repository.UserRepository;
import com.byteforge.bot.dto.ChatRequest;
import com.byteforge.bot.dto.ChatResponse;
import com.byteforge.bot.model.ChatMessage;
import com.byteforge.bot.repository.ChatMessageRepository;
import com.byteforge.bot.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody ChatRequest chatRequest,
            @AuthenticationPrincipal UserDetails userDetails
            ){

        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));

        ChatResponse chatResponse = chatService.processMessage(user.getId(), topic, chatRequest);

        return ResponseEntity.ok(chatResponse);
    }

    @GetMapping("/{topic}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String topic, @AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null){
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));

        List<ChatMessage> messages = chatMessageRepository.findByUserIdAndTopicIdOrderByTimestampAsc(user.getId(), topic);
        return ResponseEntity.ok(messages);

    }

}
