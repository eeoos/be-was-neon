package webserver.http.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.FileUtils;
import webserver.http.cookie.Cookie;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class);
    public static final char COLON = ':';
    public static final char QUESTION_MARK = '?';
    public static final String AND = "&";
    public static final char EQUAL = '=';
    public static final String CRLF = "\r\n";
    public static final String DOUBLE_CRLF = "\r\n\r\n";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String COOKIE = "Cookie";
    public static final String SEMI_COLON = ";";

    public static HttpRequest parse(InputStream in) throws IOException {

        String headerString = readHeaderString(in);

        String[] lines = headerString.split(CRLF);
        if (lines.length == 0) {
            throw new IllegalArgumentException("invalid HTTP request");
        }

        RequestLine requestLine = parseRequestLine(lines[0]);
        Map<String, List<String>> headers = parseHeaders(lines);
        RequestTarget requestTarget = splitRequestTarget(requestLine.path);

        Map<String, Cookie> cookies = parseCookies(headers);

        // body
        PostRequestResult postResult = new PostRequestResult(
                null,
                new HashMap<>()
        );

        if ("POST".equalsIgnoreCase(requestLine.method)) {
            postResult = processPostRequest(in, headers);
        }

        return new HttpRequest(
                requestLine.method,
                requestTarget.path,
                requestLine.protocol,
                headers,
                requestTarget.parameters,
                postResult.parameters,
                postResult.body,
                cookies
        );
    }

    private static Map<String, Cookie> parseCookies(Map<String, List<String>> headers) {
        Map<String, Cookie> cookies = new HashMap<>();

        if (headers.containsKey(COOKIE)) {
            String cookieHeader = headers.get(COOKIE).get(0);
            String[] cookiePairs = cookieHeader.split(SEMI_COLON);

            for (String pair : cookiePairs) {
                String[] keyValue = pair.trim().split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    cookies.put(key, new Cookie(key, value));
                }
            }
        }
        return cookies;
    }

    private static String readHeaderString(InputStream in) throws IOException {
        byte[] buffer = new byte[1];
        StringBuilder headerBuilder = new StringBuilder();

        boolean foundHeaderEnd = false;
        while (!foundHeaderEnd) {
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {

                throw new IOException("스트림의 끝에 도달했습니다.");
            }

            headerBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));

            if (headerBuilder.length() >= 4 &&
                    headerBuilder.substring(headerBuilder.length() - 4).equals(DOUBLE_CRLF)) {
                foundHeaderEnd = true;
            }
        }

        return headerBuilder.toString();
    }

    private static PostRequestResult processPostRequest(InputStream in,
                                                        Map<String, List<String>> headers) throws IOException {
        String body = null;
        Map<String, List<String>> bodyParameters = new HashMap<>();

        if (headers.containsKey(CONTENT_LENGTH)) {
            int contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH).get(0));

            if (contentLength > 0) { // body가 있는 경우
                body = readBody(in, contentLength);
                logger.debug(body);
                if (isFormUrlEncoded(headers)) {
                    bodyParameters = parseQueryString(body);
                }
            }
        }

        return new PostRequestResult(body, bodyParameters);
    }

    private static Map<String, List<String>> parseQueryString(String queryString) {
        Map<String, List<String>> parameters = new HashMap<>();
        String[] pairs = queryString.split(AND);

        for (String pair : pairs) {
            int equalPos = pair.indexOf(EQUAL);
            if (equalPos < 0) {
                throw new IllegalArgumentException("유효하지 않은 파라미터 형식: " + pair);
            }

            String key = pair.substring(0, equalPos);
            String value = pair.substring(equalPos+1);

            key = FileUtils.decodeUrl(key);
            value = FileUtils.decodeUrl(value);

            parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return parameters;
    }

    private static String readBody(InputStream in, int contentLength) throws IOException {
        byte[] buffer = new byte[contentLength];
        int bytesRead = 0;
        int totalBytesRead = 0;

        while (totalBytesRead < contentLength) {
            bytesRead = in.read(buffer, totalBytesRead, contentLength - totalBytesRead);
            if(bytesRead == -1) break;
            totalBytesRead += bytesRead;
        }

        return new String(buffer, 0, totalBytesRead, StandardCharsets.UTF_8);
    }

    private static boolean isFormUrlEncoded(Map<String, List<String>> headers) {
        if (headers.containsKey(CONTENT_TYPE)) {
            String contentType = headers.get(CONTENT_TYPE).get(0);
            return contentType.toLowerCase().startsWith(APPLICATION_X_WWW_FORM_URLENCODED);
        }
        return false;
    }

    private static RequestLine parseRequestLine(String line)  {
        String[] requestLineArr = line.split(" ");

        if (requestLineArr.length < 3) {
            throw new IllegalArgumentException("Invalid request line format");
        }
        logger.debug(line);

        String method = requestLineArr[0];
        String path = requestLineArr[1];
        String protocol = requestLineArr[2];
        return new RequestLine(method, path, protocol);
    }

    private static Map<String, List<String>> parseHeaders(String[] lines)  {
        Map<String, List<String>> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) { // 2번째 줄부터
            String line = lines[i];

            if (line.isEmpty()) {
                break;
            }

            int colonPos = line.indexOf(COLON);
            if (colonPos > 0) {
                String headerName = line.substring(0, colonPos).trim();
                String headerValue = line.substring(colonPos + 1).trim();

                logger.debug("{}: {}", headerName, headerValue);
                headers.computeIfAbsent(headerName, k -> new ArrayList<>()).add(headerValue);
            } else {
                throw new IllegalArgumentException("invalid request header format: " + line);
            }
        }
        return headers;
    }

    private static RequestTarget splitRequestTarget(String requestTarget) {
        HashMap<String, List<String>> parameters = new HashMap<>();
        int questionMarkPos = requestTarget.indexOf(QUESTION_MARK);
        String path = requestTarget;

        if (questionMarkPos != -1) { // ?가 있는 경우
            path = requestTarget.substring(0, questionMarkPos); // 순수 경로 추출

            if (questionMarkPos < requestTarget.length() - 1) {
                String queryString = requestTarget.substring(questionMarkPos + 1);
                if (!queryString.isEmpty()) { // ? 뒤에 문자가 있고 비어있지 않은 경우
                    extractParameters(queryString, parameters);
                }
            }
        }

        return new RequestTarget(
                path,
                parameters
        );
    }

    private static void extractParameters(String queryString, HashMap<String, List<String>> parameters) {
        String[] pairs = queryString.split(AND);
        for (String pair : pairs) {
            int equalPos = pair.indexOf(EQUAL);
            if (equalPos < 0) {
                throw new IllegalArgumentException("Invalid request parameters format: Missing equal");
            }

            String key = pair.substring(0, equalPos);
            String value = pair.substring(equalPos + 1);

            key = FileUtils.decodeUrl(key);
            value = FileUtils.decodeUrl(value);

            parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private record RequestLine(String method, String path, String protocol) { }

    private record RequestTarget(String path, Map<String, List<String>> parameters) { }

    private record PostRequestResult(String body, Map<String, List<String>> parameters) { }
}
