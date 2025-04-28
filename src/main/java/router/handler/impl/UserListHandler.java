package router.handler.impl;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtils;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class UserListHandler extends StaticFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserListHandler.class);
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            logger.info("로그인 또는 회원가입 후 이용해주세요.");
            response.send401();
            return;
        }

        byte[] body = FileUtils.readFileBytes("/user/index.html");
        String content = new String(body, StandardCharsets.UTF_8);
        content = listUpUsers(content);
        content = replaceHeaderWithLoggedInUser(content, user.getName());

        response.sendOk(ContentType.HTML, content.getBytes(StandardCharsets.UTF_8));
    }

    private String listUpUsers(String content) {
        Collection<User> users = Database.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<tbody>");
        for (User user : users) {
            sb.append(String.format("<tr>\n" +
                    "    <td>%s</td>\n" +
                    "    <td>%s</td>\n" +
                    "    <td>%s</td>\n" +
                    "</tr>", user.getUserId(), user.getName(), user.getEmail()));
        }
        sb.append("</tbody>");

        return content.replaceAll("<tbody></tbody>", sb.toString());
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

        return content.replaceAll("<ul class=\"header__menu\">\n" +
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
}
