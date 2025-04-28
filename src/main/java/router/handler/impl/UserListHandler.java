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
        logger.info(content);
        content = replaceHeaderWithLoggedInUser(content, user.getName());
        content = listUpUsers(content);

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
}
