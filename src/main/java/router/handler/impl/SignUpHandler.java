package router.handler.impl;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import webserver.common.ContentType;
import webserver.common.HttpStatus;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SignUpHandler implements HttpRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SignUpHandler.class);
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        // 기존의 handleUserCreation 메서드 로직을 그대로 복사
        Map<String, List<String>> parameters = request.getBodyParameters();

        if (!containsRequiredParameters(parameters)) {
            response.send(HttpStatus.BAD_REQUEST, ContentType.HTML,
                    "필수 회원 정보가 누락되었습니다".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String userId = parameters.get("userId").get(0);
        String password = parameters.get("password").get(0);
        String name = parameters.get("name").get(0);
        String email = parameters.get("email").get(0);

        logger.debug("sign up request - userId: {}, password: {}, name: {}, email: {}",
                userId, password, name, email);

        if (Database.findUserById(userId) != null) {
            logger.info("이미 사용 중인 사용자 ID: {}", userId);
            response.send409();
            return;
        }

        User user = new User(userId, password, name, email);
        Database.addUser(user);

        HttpSession session = request.getSession();
        session.setAttributes("user", user);

        response.addSessionCookie(session.getId());
        response.sendRedirect("/index.html");
    }

    private boolean containsRequiredParameters(Map<String, List<String>> parameters) {
        return parameters.containsKey("userId") &&
                parameters.containsKey("password") &&
                parameters.containsKey("name") &&
                parameters.containsKey("email");
    }
}
