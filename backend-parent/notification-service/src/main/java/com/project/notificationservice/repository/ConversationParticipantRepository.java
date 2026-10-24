package com.project.notificationservice.repository;

import com.project.notificationservice.entity.ConversationParticipant;
import com.project.notificationservice.entity.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {
}
