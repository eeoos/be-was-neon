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

import static webserver.http.session.HttpSession.REDIRECT_URL_SESSION_KEY;

public class UserListHandler extends HtmlFileHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserListHandler.class);
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            logger.info("유저 목록은 회원만 조회할 수 있습니다. 로그인 또는 회원가입 후 이용해주세요.");
            session.setAttributes(REDIRECT_URL_SESSION_KEY, request.getRequestTarget());
            response.addSessionCookie(session.getId());

            response.send401();
            return;
        }

        byte[] fileContent = loadFileContent("/user/index.html");
        byte[] processedContent = processContent(fileContent, ContentType.HTML, request);

        String htmlContent = new String(processedContent, StandardCharsets.UTF_8);
        htmlContent = listUpUsers(htmlContent);

        response.sendOk(ContentType.HTML, htmlContent.getBytes(StandardCharsets.UTF_8));
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
