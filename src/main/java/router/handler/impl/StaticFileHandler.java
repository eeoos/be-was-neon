package router.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;

import java.io.IOException;

public class StaticFileHandler extends BaseFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    @Override
    protected byte[] processContent(byte[] content, ContentType contentType, HttpRequest request){
        if (contentType == ContentType.HTML) {
            logger.warn("HTML 파일은 HtmlFileHandler에서 처리애햐 합니다: {}", request.getPath());
        }
        return content;
    }
}
