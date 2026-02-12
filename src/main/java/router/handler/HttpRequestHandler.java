package router.handler;

import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;

public interface HttpRequestHandler {
    void handle(HttpRequest request, HttpResponse response) throws IOException;
}
