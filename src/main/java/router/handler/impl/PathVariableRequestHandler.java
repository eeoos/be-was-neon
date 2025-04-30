package router.handler.impl;

import router.handler.HttpRequestHandler;
import webserver.http.request.HttpRequest;
import webserver.http.request.PathVariableRequestWrapper;

import java.util.Map;

public class PathVariableRequestHandler implements HttpRequestHandler {
    private final HttpRequestHandler delegate;
    private final Map<String, String> pathVariables;

    public PathVariableRequestHandler(HttpRequestHandler delegate, Map<String, String> pathVariables) {
        this.delegate = delegate;
        this.pathVariables = pathVariables;
    }

    @Override
    public void handle(HttpRequest request, webserver.http.response.HttpResponse response) throws java.io.IOException {
        // 경로 변수를 포함한 요청 래퍼 생성
        PathVariableRequestWrapper requestWrapper = new PathVariableRequestWrapper(request, pathVariables);

        delegate.handle(requestWrapper, response);
    }
}
