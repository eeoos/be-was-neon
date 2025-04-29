package webserver.http.request;

import webserver.http.cookie.Cookie;
import webserver.http.session.HttpSession;
import webserver.http.session.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {

    public static final String SID = "SID";
    private final String method;
    private final String path;
    private final String protocol;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParameters;
    private final Map<String, List<String>> bodyParameters;
    private final String body;
    private final Map<String, Cookie> cookies;
    private HttpSession session;

    public HttpRequest(String method,
                       String path,
                       String protocol,
                       Map<String, List<String>> headers,
                       Map<String, List<String>> queryParameters,
                       Map<String, List<String>> bodyParameters,
                       String body,
                       Map<String, Cookie> cookies) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.queryParameters = queryParameters;
        this.bodyParameters = bodyParameters;
        this.body = body;
        this.cookies = cookies;

        this.session = getOrCreateSession();
    }

    private HttpSession getOrCreateSession() {
        String sessionId = null;
        if (cookies.containsKey(SID)) {
            sessionId = cookies.get(SID).getValue();
        }
        return SessionManager.getInstance().getSession(sessionId);
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getQueryParameters() {
        Map<String, List<String>> copy = queryParameters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue()) // value인 List를 불변으로
                ));

        return Collections.unmodifiableMap(copy); // Map을 불변으로
    }

    public Map<String, List<String>> getBodyParameters() {
        Map<String, List<String>> copy = bodyParameters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue()) // value인 List를 불변으로
                ));

        return Collections.unmodifiableMap(copy); // Map을 불변으로
    }

    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> copy = headers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.unmodifiableList(e.getValue()) // value인 List를 불변으로
                ));

        return Collections.unmodifiableMap(copy); // Map을 불변으로
    }

    public Map<String, Cookie> getCookies() {
        return Collections.unmodifiableMap(cookies);
    }

    public String getBody() {
        return body;
    }

    public String getMethod() {
        return method;
    }

    public String getQueryParametersStr() {
        Map<String, List<String>> queryParams = getQueryParameters();

        if (queryParams.isEmpty() || queryParams == null) {
            return "";
        }

        return queryParams.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> entry.getKey() + "=" + value))
                .collect(Collectors.joining("&"));
    }

    public String getRequestTarget() {
        String queryParametersStr = getQueryParametersStr();
        if (queryParametersStr.isEmpty()) {
            return path;
        }
        return path + "?" + queryParametersStr;
    }

    public HttpSession getSession() {
        return session;
    }

    public String getProtocol() {
        return protocol;
    }
}
