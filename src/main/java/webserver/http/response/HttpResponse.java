package webserver.http.response;

import webserver.common.ContentType;
import util.FileUtils;
import webserver.common.HttpStatus;
import webserver.http.cookie.Cookie;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse {
    public static final String CRLF = "\r\n";
    public static final String HTTP_1_1 = "HTTP/1.1";
    public static final String LOCATION = "Location";
    public static final String ALLOW = "Allow";
    public static final String SET_COOKIE = "Set-Cookie";

    private final DataOutputStream dos;
    private HttpStatus status;
    private ContentType contentType;
    private final Map<String, List<String>> headers = new HashMap<>();
    private byte[] body;
    private final List<Cookie> cookies = new ArrayList<>();

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
        this.status = HttpStatus.OK;
        this.contentType = ContentType.HTML;
    }

    public void send401() throws IOException {
        byte[] body = FileUtils.readFileBytes("/login/index.html");
        status(HttpStatus.UNAUTHORIZED)
                .contentType(ContentType.HTML)
                .addHeaders("WWW-Authenticate", "Basic realm=\"User Authentication\"")
                .body(body)
                .send();

    }

    public void send(HttpStatus status, ContentType contentType, byte[] body) throws IOException {
        status(status)
                .contentType(contentType)
                .body(body)
                .send();
    }

    public void sendLoginFail(String path) throws IOException {
        byte[] body = FileUtils.readFileBytes(path);
        status(HttpStatus.BAD_REQUEST)
                .contentType(ContentType.HTML)
                .body(body)
                .send();
    }

    public void sendOk(ContentType contentType, byte[] body) throws IOException {
        status(HttpStatus.OK)
                .contentType(contentType)
                .body(body)
                .send();
    }

    public void sendRedirect(String location) throws IOException {
        status(HttpStatus.FOUND)
                .addHeaders(LOCATION, location)
                .send();
    }

    public void send403() throws IOException {
        byte[] errorBody = FileUtils.readFileBytes("/errors/403.html");
        status(HttpStatus.FORBIDDEN)
                .contentType(ContentType.HTML)
                .body(errorBody)
                .send();
    }
    public void send404() throws IOException {
        byte[] errorBody = FileUtils.readFileBytes("/errors/404.html");
        status(HttpStatus.NOT_FOUND)
                .contentType(ContentType.HTML)
                .body(errorBody)
                .send();
    }

    public void send405(String allowedMethods) throws IOException {
        byte[] errorBody = FileUtils.readFileBytes("/errors/405.html");
        String errorHtml = new String(errorBody).replace("{ALLOWED_METHODS}", allowedMethods);

        status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(ContentType.HTML)
                .addHeaders(ALLOW, allowedMethods)
                .body(errorHtml.getBytes())
                .send();
    }

    public void send409() throws IOException {
        byte[] errorBody = FileUtils.readFileBytes("/errors/409.html");
        status(HttpStatus.CONFLICT)
                .contentType(ContentType.HTML)
                .body(errorBody)
                .send();
    }

    public void send500() throws IOException {
        byte[] errorBody = FileUtils.readFileBytes("/errors/500.html");
        status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(ContentType.HTML)
                .body(errorBody)
                .send();
    }

    private void send() throws IOException {
        writeStatusLine();
        writeHeaders();
        writeBody();
    }

    private void writeStatusLine() throws IOException {
        dos.writeBytes(String.format(HTTP_1_1 + " %d %s" + CRLF, status.getStatusCode(), status.getReasonPhrase()));
    }

    private void writeHeaders() throws IOException {
        if (contentType != null) {
            dos.writeBytes(String.format("Content-Type: %s" + CRLF, contentType.getMineType()));
        }

        if (body != null) {
            dos.writeBytes(String.format("Content-Length: %d" + CRLF, body.length));
        }

        for (var header : headers.entrySet()) {
            dos.writeBytes(String.format("%s: %s" + CRLF, header.getKey(), String.join(",", header.getValue())));
        }

        for (Cookie cookie : cookies) {
            dos.writeBytes(String.format("%s: %s" + CRLF, SET_COOKIE, cookie.toString()));
        }

        dos.writeBytes(CRLF);
    }

    private void writeBody() throws IOException {
        if (body != null && body.length > 0) {
            dos.write(body, 0, body.length);
            dos.flush();
        }
    }

    private HttpResponse status(HttpStatus status) {
        this.status = status;
        return this;
    }

    private HttpResponse contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    private HttpResponse body(byte[] body) {
        this.body = body;
        return this;
    }

    public HttpResponse addHeaders(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return this;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void addSessionCookie(String sessionId) {
        Cookie cookie = new Cookie("SID", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        addCookie(cookie);
    }
}
