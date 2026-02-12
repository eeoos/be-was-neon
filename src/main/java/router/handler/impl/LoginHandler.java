package router.handler.impl;

import db.Database;
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

public class LoginHandler implements HttpRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, List<String>> parameters = request.getBodyParameters();

        if (!containsRequiredParameters(parameters)) {
            response.sendLoginFail("/login/fail/not-required-parameters.html");
            return;
        }

        String userId = parameters.get("userId").get(0);
        String password = parameters.get("password").get(0);

        logger.debug("login request - userId: {}, password: {}", userId, password);

        User user = Database.findUserById(userId);

        if (user == null) {
            logger.info("login fail - non-existent user");
            response.sendLoginFail("/login/fail/not-found-user.html");
            return;
        }

        if (!user.getPassword().equals(password)) {
            logger.info("login fail - wrong password");
            response.sendLoginFail("/login/fail/wrong-password.html");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttributes("user", user);

        response.addSessionCookie(session.getId());
        String redirectUrl = (String) session.getAttribute(HttpSession.REDIRECT_URL_SESSION_KEY);
        logger.debug("redirectUrl: {}", redirectUrl);
        if (redirectUrl != null) {
            response.sendRedirect(redirectUrl);
            return;
        }
        response.sendRedirect("/index.html");
    }

    private boolean containsRequiredParameters(Map<String, List<String>> parameters) {
        return parameters.containsKey("userId") &&
                parameters.containsKey("password");
    }
}
