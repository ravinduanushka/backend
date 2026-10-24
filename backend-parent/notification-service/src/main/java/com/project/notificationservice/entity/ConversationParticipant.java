package com.project.notificationservice.entity;

import com.project.notificationservice.util.TimeUtil;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant {

    @EmbeddedId
    private ConversationParticipantId id;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private String joinedAt;

    protected ConversationParticipant() {
    }

    public ConversationParticipant(String conversationId, String userId) {
        this.id = new ConversationParticipantId(conversationId, userId);
    }

    @PrePersist
    void onCreate() {
        joinedAt = TimeUtil.now();
    }

    public ConversationParticipantId getId() { return id; }
    public String getJoinedAt() { return joinedAt; }
}
