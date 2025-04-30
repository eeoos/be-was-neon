package model;

import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String userId;
    public Article(String userId, String content) {
        this.content = content;
        this.userId = userId;
    }

    public Article(Long id,  String userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
