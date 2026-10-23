package com.project.mentorservice.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "expertises")
public class Expertise {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "category_id", nullable = false, length = 36)
    private String categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() { return id; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}