package router.handler.impl;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import util.FileUtils;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StaticFileHandler implements HttpRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String path = request.getPath();

        if (path.equals("/") || path.equals("/?")) {
            path = "/index.html";
        }

        ContentType contentType = ContentType.getContentTypeByPath(path);

        try {
            byte[] body = FileUtils.readFileBytes(path);

            if (contentType == ContentType.HTML) {
                String content = new String(body, StandardCharsets.UTF_8);

                HttpSession session = request.getSession();
                User user = (User) session.getAttribute("user");

                if (user != null) {
                    content = replaceHeaderWithLoggedInUser(content, user.getName());
                }
                body = content.getBytes(StandardCharsets.UTF_8);
            }

            response.sendOk(contentType, body);
        } catch (IOException e) {
            logger.error("파일을 찾을 수 없습니다: {}", path);
            response.send404();
        }
    }

    protected String replaceHeaderWithLoggedInUser(String content, String name) {
        String loggedInHeader =
                "<ul class=\"header__menu\">\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "            <span>안녕하세요, " + name + "님</span>\n" +
                        "          </li>\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "            <a class=\"btn btn_ghost btn_size_s\" href=\"/user/list\">사용자 목록</a>\n" +
                        "          </li>\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "<form action=\"/user/logout\" method=\"post\">\n" +
                        "              <button type=\"submit\" class=\"btn btn_contained btn_size_s\">로그아웃</button>\n" +
                        "            </form>" +
                        "          </li>\n" +
                        "        </ul>";

        String startLine = "<ul class=\"header__menu\">"; // 교체할 부분의 시작 라인
        String endLine = "</ul>";

        int startIndex = content.indexOf(startLine);
        if(startIndex == -1) return content;

        int endIndex = content.indexOf(endLine);
        if(endIndex == -1) return content;

        return content.substring(0, startIndex) + loggedInHeader + content.substring(endIndex);
    }
}
