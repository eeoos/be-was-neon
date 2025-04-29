package router.handler.impl;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.session.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HtmlFileHandler extends BaseFileHandler{
    private static final Logger logger = LoggerFactory.getLogger(HtmlFileHandler.class);
    @Override
    protected byte[] processContent(byte[] content, ContentType contentType, HttpRequest request) throws IOException {
        if (contentType != ContentType.HTML) {
            logger.warn("HTML이 아닌 파일은 StaticResourceHandler에서 처리해야 합니다: {}", request.getPath());
            return content;
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        String htmlContent = new String(content, StandardCharsets.UTF_8);

        if (user != null) {
            htmlContent = replaceHeaderWithLoggedInUser(htmlContent, user.getName());
        }

        return htmlContent.getBytes(StandardCharsets.UTF_8);
    }

    protected String replaceHeaderWithLoggedInUser(String content, String name) {
        String loggedInHeader =
                "<ul class=\"header__menu\">\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "            <span>안녕하세요, " + name + "님</span>\n" +
                        "          </li>\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "            <a class=\"btn btn_contained btn_size_s\" href=\"/user/list\">사용자 목록</a>\n" +
                        "          </li>\n" +
                        "          <li class=\"header__menu__item\">\n" +
                        "<form action=\"/user/logout\" method=\"post\">\n" +
                        "              <button type=\"submit\" class=\"btn btn_contained btn_size_s\">로그아웃</button>\n" +
                        "            </form>" +
                        "          </li>\n" +
                        "        </ul>";

        String startLine = "<ul class=\"header__menu\">"; // 교체할 부분의 시작 라인
        String endLine = "</ul>"; // 끝 라인

        int startIndex = content.indexOf(startLine);
        if(startIndex == -1) return content;

        int endIndex = content.indexOf(endLine);
        if(endIndex == -1) return content;

        return content.substring(0, startIndex) + loggedInHeader + content.substring(endIndex);
    }
}
