package router.handler.impl;

import router.handler.HttpRequestHandler;
import webserver.http.cookie.Cookie;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.session.HttpSession;

import java.io.IOException;

public class LogoutHandler implements HttpRequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("SID", "");
        cookie.setMaxAge(0); // 만료
        response.addCookie(cookie);

        response.sendRedirect("/index.html");
    }
}
