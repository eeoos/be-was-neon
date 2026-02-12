package router.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import util.FileUtils;
import webserver.common.ContentType;
import webserver.common.HttpMethod;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodNotAllowHandler implements HttpRequestHandler {
    private final String allowedMethods;

    public MethodNotAllowHandler(Set<HttpMethod> allowedMethods) {
        this.allowedMethods = allowedMethods.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        response.send405(allowedMethods);
    }
}
