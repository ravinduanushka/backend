package com.project.notificationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ConversationParticipantId implements Serializable {

    @Column(name = "conversation_id", length = 36)
    private String conversationId;

    @Column(name = "user_id", length = 36)
    private String userId;

    protected ConversationParticipantId() {
    }

    public ConversationParticipantId(String conversationId, String userId) {
        this.conversationId = conversationId;
        this.userId = userId;
    }

    public String getConversationId() { return conversationId; }
    public String getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationParticipantId other)) {
            return false;
        }
        return Objects.equals(conversationId, other.conversationId) && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, userId);
    }
}
