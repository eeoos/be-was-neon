package webserver.http.session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    private final String id;
    private final Map<String, Object> attributes = new HashMap<>();
    private final long creationTime;
    private long lastAccessTime;
    public static final String REDIRECT_URL_SESSION_KEY = "REDIRECT_AFTER_LOGIN_URL";

    public HttpSession() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = creationTime;
    }

    public void setAttributes(String name, Object value) {
        attributes.put(name, value);
    }

    public void access() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void invalidate() {
        attributes.clear();
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
}
