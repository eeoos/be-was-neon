package webserver.http.request;

public class HttpRequestWrapper extends HttpRequest {
    private final String remappedPath;
    private final HttpRequest originalRequest;

    public HttpRequestWrapper(HttpRequest originalRequest, String remappedPath) {
        super(
                originalRequest.getMethod(),
                remappedPath,
                originalRequest.getProtocol(),
                originalRequest.getHeaders(),
                originalRequest.getQueryParameters(),
                originalRequest.getBodyParameters(),
                originalRequest.getBody(),
                originalRequest.getCookies()
        );
        this.remappedPath = remappedPath;
        this.originalRequest = originalRequest;
    }

    @Override
    public String getPath() {
        return remappedPath;
    }
}
