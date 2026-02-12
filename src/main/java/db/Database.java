package db;

import model.Article;
import model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Map<String, User> users = new HashMap<>();
    private static Map<Long, Article> articles = new HashMap<>();
    private static long userSeq = 0L;
    private static long articleSeq = 0L;

    public static void addArticle(Article article) {
        article.setId(++articleSeq);
        articles.put(article.getId(), article);
    }

    public static Article findArticleById(Long articleId) {
        return articles.get(articleId);
    }

    public static Collection<Article> findAllArticle() {
        return articles.values();
    }

    public static Article findPreviousArticle(Long currentId) {
        Long previousId = null;
        for (Long id : articles.keySet()) {
            if (id < currentId && (previousId == null || id > previousId)) {
                previousId = id;
            }
        }
        return previousId != null ? articles.get(previousId) : null;
    }

    public static Article findNextArticle(Long currentId) {
        Long nextId = null;
        for (Long id : articles.keySet()) {
            if (id > currentId && (nextId == null || id < nextId)) {
                nextId = id;
            }
        }
        return nextId != null ? articles.get(nextId) : null;
    }

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
