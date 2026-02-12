package webserver.http.request;

import java.util.Collections;
import java.util.Map;

public class PathVariableRequestWrapper extends HttpRequest {
    private final Map<String, String> pathVariables;
    private final HttpRequest originalRequest;

    public PathVariableRequestWrapper(HttpRequest originalRequest, Map<String, String> pathVariables) {
        super(
                originalRequest.getMethod(),
                originalRequest.getPath(),
                originalRequest.getProtocol(),
                originalRequest.getHeaders(),
                originalRequest.getQueryParameters(),
                originalRequest.getBodyParameters(),
                originalRequest.getBody(),
                originalRequest.getCookies()
        );
        this.pathVariables = Collections.unmodifiableMap(pathVariables);
        this.originalRequest = originalRequest;
    }

    public String getPathVariable(String name) {
        return pathVariables.get(name);
    }

    public Map<String, String> getPathVariables() {
        return pathVariables;
    }
}
