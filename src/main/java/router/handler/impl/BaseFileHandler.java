package router.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import util.FileUtils;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;

public abstract class BaseFileHandler implements HttpRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(BaseFileHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String path = normalizePath(request.getPath());
        ContentType contentType = ContentType.getContentTypeByPath(path);

        try {
            byte[] fileContent = loadFileContent(path);
            byte[] processedContent = processContent(fileContent, contentType, request);
            response.sendOk(contentType, processedContent);
        } catch (IOException e) {
            logger.error("파일을 찾을 수 없습니다: {}", path);
            response.send404();
        }
    }

    protected String normalizePath(String path) {
        if (path.equals("/") || path.equals("/?")) {
            return "/index.html";
        }
        return path;
    }

    protected byte[] loadFileContent(String path) throws IOException {
        return FileUtils.readFileBytes(path);
    }

    protected abstract byte[] processContent(byte[] content, ContentType contentType, HttpRequest request) throws IOException;
}
