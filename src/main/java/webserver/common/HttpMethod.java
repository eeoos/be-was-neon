package webserver.common;

public enum HttpMethod {
    POST, PATCH, PUT, GET, DELETE;

    public static HttpMethod from(String method) {
        return valueOf(method);
    }
}
