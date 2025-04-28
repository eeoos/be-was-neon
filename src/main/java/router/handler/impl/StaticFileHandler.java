package router.handler.impl;

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
                content = processDynamicHtml(content, request);
                body = content.getBytes(StandardCharsets.UTF_8);
            }

            response.sendOk(contentType, body);
        } catch (IOException e) {
            logger.error("파일을 찾을 수 없습니다: {}", path);
            response.send404();
        }
    }

    private String processDynamicHtml(String content, HttpRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            String loggedInHeader =
                    "<ul class=\"header__menu\">\n" +
                            "          <li class=\"header__menu__item\">\n" +
                            "            <span>안녕하세요, " + user.getName() + "님</span>\n" +
                            "          </li>\n" +
                            "          <li class=\"header__menu__item\">\n" +
                            "<form action=\"/user/logout\" method=\"post\">\n" +
                            "              <button type=\"submit\" class=\"btn btn_contained btn_size_s\">로그아웃</button>\n" +
                            "            </form>" +
                            "          </li>\n" +
                            "        </ul>";

            content = content.replaceAll("<ul class=\"header__menu\">\n" +
                    "          <li class=\"header__menu__item\">\n" +
                    "            <a class=\"btn btn_contained btn_size_s\" href=\"/login/index.html\">로그인</a>\n" +
                    "          </li>\n" +
                    "          <li class=\"header__menu__item\">\n" +
                    "            <a class=\"btn btn_ghost btn_size_s\" href=\"/registration/index.html\">\n" +
                    "              회원 가입\n" +
                    "            </a>\n" +
                    "          </li>\n" +
                    "        </ul>", loggedInHeader);
        }
        return content;
    }
}
