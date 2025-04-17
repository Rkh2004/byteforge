package com.byteforge.bot.service;

import com.byteforge.auth.model.User;
import com.byteforge.bot.dto.ChatRequest;
import com.byteforge.bot.dto.ChatResponse;
import com.byteforge.bot.model.ChatMessage;
import com.byteforge.bot.model.SenderType;
import com.byteforge.bot.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient.Builder chatClientBuilder;

    public ChatService(ChatMessageRepository chatMessageRepository,ChatClient.Builder chatClientBuilder) {

        this.chatMessageRepository = chatMessageRepository;
        this.chatClientBuilder = chatClientBuilder;
    }
    @Value("${byteforge.chat.history.limit:10}")
    private int historyLimit;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are ByteForge AI, a friendly and helpful programming tutor for the ByteForge Java learning environment.
            Your personality is enthusiastic, patient, and encouraging, like the 'Head First Java' book style.
            Your primary goal is to help absolute beginners understand Java concepts.

            RULES:
            1.  **Explain concepts in a VERY simple, beginner-friendly way.** Use analogies, simple metaphors, and real-world examples relevant to beginners.
            2.  **Keep code examples short, simple, and directly related to the concept.** Avoid advanced syntax unless explaining it specifically.
            3.  **Base your answers PRIMARILY on the provided "Static Content" for the current topic ({topic}).**
            4.  **If the user asks about something complex or outside the provided context, gently guide them back** by saying something like, "That's a great question for later! Let's focus on understanding the basics of {topic} from the material here first," or state you can only answer based on the current topic's material.
            5.  **Acknowledge the user's question** before answering.
            6.  **Do NOT invent Java features or concepts.** Stick to standard Java as explained in the context.
            7.  **Format your answers clearly** using markdown (like code blocks for `code`, **bold** for emphasis).
            8.  **If the user asks something not related to Java Programming, guide them back** by saying please asks something about Java.

            PROVIDED CONTEXT:
            ---
            **Static Content for Topic: {topic}**
            {staticContent}
            ---
            """;


    @Transactional
    public ChatResponse processMessage(User user, String topic, ChatRequest chatRequest){

        saveChatMessage(user, topic, chatRequest.getQuery(), SenderType.USER);

        List<Message> historyMessages = getHistoryMessages(user, topic);

        Prompt prompt = buildPrompt(topic, chatRequest.getStaticContent(), historyMessages,chatRequest.getQuery());

        ChatClient chatClient = chatClientBuilder.build();

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt(prompt).call().chatResponse();

        String aiContent = aiResponse.getResult().getOutput().getText();

        log.info("Received AI response for user: {}, topic: {}", user, topic);

        saveChatMessage(user, topic, aiContent, SenderType.AI);

        return new ChatResponse(aiContent, Instant.now());

    }

    private void saveChatMessage(User user, String topic, String content, SenderType senderType){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setTopicId(topic);
        chatMessage.setMessageContent(content);
        chatMessage.setSenderType(senderType);

        chatMessageRepository.save(chatMessage);
        log.debug("Saved {} message for user: {}, topic: {}", senderType, user, topic);
    }

    private List<Message> getHistoryMessages(User user, String topic) {
        Pageable pageable = PageRequest.of(0, historyLimit); // Fetch latest N messages
        List<ChatMessage> history = chatMessageRepository.findByUserIdAndTopicIdOrderByTimestampDesc(user.getId(), topic, pageable);

        // Convert stored messages to Spring AI Message format, reversing order to chronological
        return history.stream()
                .map(msg -> (Message)(msg.getSenderType() == SenderType.USER
                        ? new UserMessage(msg.getMessageContent())
                        : new AssistantMessage(msg.getMessageContent())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            java.util.Collections.reverse(list); // Ensure chronological order for the prompt
                            return list;
                        }
                ));
    }

    private Prompt buildPrompt(String topic, String staticContent,List<Message> historyMessages, String query){
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT_TEMPLATE);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of(
                "topic", topic,
                "staticContent", staticContent
        ));
        List<Message> allMessages = new ArrayList<>();

        allMessages.add(systemMessage);
        allMessages.addAll(historyMessages);
        allMessages.add(new UserMessage(query));

        return new Prompt(allMessages);
    }


}
