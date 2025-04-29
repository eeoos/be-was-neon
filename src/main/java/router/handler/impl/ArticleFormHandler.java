package router.handler.impl;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.request.HttpRequest;
import webserver.http.request.HttpRequestWrapper;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;

import static webserver.http.session.HttpSession.REDIRECT_URL_SESSION_KEY;

public class ArticleFormHandler extends StaticFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(ArticleFormHandler.class);
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            logger.info("글쓰기는 회원 권한입니다. 로그인 또는 회원가입 후 이용해주세요.");
            session.setAttributes(REDIRECT_URL_SESSION_KEY, request.getRequestTarget());
            response.addSessionCookie(session.getId()); // 이렇게 구현했을 시 세션 타임아웃이 필수적으로 보임

            response.send401();
            return;
        }

        HttpRequest remappedRequest = new HttpRequestWrapper(request, "/article/index.html");

        super.handle(remappedRequest, response);
    }
}
