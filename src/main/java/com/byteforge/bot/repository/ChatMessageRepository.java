package com.byteforge.bot.repository;

import com.byteforge.bot.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdAndTopicIdOrderByTimestampDesc(Long userId, String topicId, Pageable pageable);
    List<ChatMessage> findByUserIdAndTopicIdOrderByTimestampAsc(Long userId, String topicId);
}
