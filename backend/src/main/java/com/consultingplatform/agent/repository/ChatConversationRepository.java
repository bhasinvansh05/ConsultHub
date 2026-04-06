package com.consultingplatform.agent.repository;

import com.consultingplatform.agent.domain.ChatConversation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByIdAndUserId(Long id, Long userId);
}
