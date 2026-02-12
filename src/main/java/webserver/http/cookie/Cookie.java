package webserver.http.cookie;

public class Cookie {
    public static final String EQUAL = "=";
    private final String name;
    private final String value;
    private int maxAge = -1;
    private String path = "/";
    private boolean httpOnly = false;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getPath() {
        return path;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public boolean isHttpOnly() {
        return this.httpOnly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(EQUAL).append(value);

        if (maxAge > -1) {
            sb.append("; Max-Age=").append(maxAge);
        }

        if (path != null) {
            sb.append("; Path=").append(path);
        }

        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }
}
