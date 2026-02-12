package router.handler.impl;

import db.Database;
import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static webserver.http.session.HttpSession.REDIRECT_URL_SESSION_KEY;

public class ArticleCreateHandler implements HttpRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCreateHandler.class);
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

        Map<String, List<String>> parameters = request.getBodyParameters();
        if (!containsRequiredParameters(parameters)) {
            response.send400("필수 입력 항목이 누락되었습니다. 내용을 입력해주세요.");
            return;
        }

        String content = parameters.get("content").get(0);

        if (content.trim().isEmpty()) {
            response.send400("내용을 입력해주세요.");
            return;
        }

        logger.debug("게시글 작성 요청 - 작성자: {}", user.getUserId());

        try {
            Article article = new Article(user.getUserId(), content);  // title 매개변수 제거
            Database.addArticle(article);

            logger.info("게시글 작성 성공: ID={}", article.getId());

            // 작성 완료 후 게시글 목록 페이지로 리다이렉트
            response.sendRedirect("/index.html");
        } catch (Exception e) {
            logger.error("게시글 작성 중 오류 발생", e);
            response.send500();
        }

    }

    private boolean containsRequiredParameters(Map<String, List<String>> parameters) {
        return parameters.containsKey("content");
    }
}
