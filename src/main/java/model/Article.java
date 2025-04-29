package model;

public class Article {
    private Long id;
    private String content;

    public Article(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
