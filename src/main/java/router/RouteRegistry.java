package router;

import router.handler.HttpRequestHandler;
import router.handler.impl.*;
import router.path.AntPathMatcher;
import webserver.common.HttpMethod;
import webserver.http.request.HttpRequest;
import webserver.http.request.PathVariableRequestWrapper;

import java.util.*;

import static webserver.common.HttpMethod.*;

public class RouteRegistry {

    private final Map<HttpMethod, Map<String, HttpRequestHandler>> routes =
            new EnumMap<>(HttpMethod.class);
    private final Map<String, Set<HttpMethod>> allowedMethods = new HashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static class RouteRegistryHolder{
        private static final RouteRegistry INSTANCE = new RouteRegistry();
    }

    public static RouteRegistry getInstance() {
        return RouteRegistryHolder.INSTANCE;
    }

    private RouteRegistry() {
        registerRoute(POST, "/user/create", new SignUpHandler());
        registerRoute(POST, "/user/login", new LoginHandler());
        registerRoute(POST, "/user/logout", new LogoutHandler());
        registerRoute(GET, "/user/list", new UserListHandler());

        registerRoute(GET, "/articles/form", new ArticleFormHandler());
        registerRoute(POST, "/articles", new ArticleCreateHandler());
        registerRoute(GET, "/articles/{id}", new ArticleReadHandler());

        registerRoute(GET, "/index.html", new MainPageHandler());
        registerRoute(GET, "/", new MainPageHandler());
    }

    public void registerRoute(HttpMethod method, String path, HttpRequestHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
        allowedMethods.computeIfAbsent(path, k -> EnumSet.noneOf(HttpMethod.class)).add(method);
    }

    public HttpRequestHandler findHandler(String method, String path) {
        try {
            HttpMethod httpMethod = from(method);
            Map<String, HttpRequestHandler> pathMap = routes.get(httpMethod);

            if (pathMap == null) {
                return null;
            }

            HttpRequestHandler exactHandler = pathMap.get(path);
            if (exactHandler != null) {
                return exactHandler;
            }

            for (Map.Entry<String, HttpRequestHandler> entry : pathMap.entrySet()) {
                String pattern = entry.getKey();

                // 경로 변수가 포함된 패턴인지 확인
                if (pattern.contains("{") && pathMatcher.isMatch(pattern, path)) {
                    Map<String, String> pathVariables = pathMatcher.extractPathVariables(pattern, path);

                    // 원래 핸들러
                    HttpRequestHandler originalHandler = entry.getValue();

                    // 경로 변수를 포함한 핸들러 래퍼 반환
                    return new PathVariableRequestHandler(originalHandler, pathVariables);
                }
            }

            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isMethodAllowed(String method, String path) {
        try {
            Set<HttpMethod> methods = allowedMethods.get(path);
            if (methods != null) {
                return methods.contains(from(method));
            }

            for (String pattern : allowedMethods.keySet()) {
                if (pattern.contains("{") && pathMatcher.isMatch(pattern, path)) {
                    methods = allowedMethods.get(pattern);
                    return methods != null && methods.contains(from(method));
                }
            }

            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasRoutesForPath(String path) {
        if (allowedMethods.containsKey(path)) {
            return true;
        }

        for (String pattern : allowedMethods.keySet()) {
            if (pattern.contains("{") && pathMatcher.isMatch(pattern, path)) {
                return true;
            }
        }

        return false;
    }

    public Set<HttpMethod> getAllowedMethods(String path) {
        Set<HttpMethod> methods = allowedMethods.get(path);
        if (methods != null) {
            return methods;
        }

        for (String pattern : allowedMethods.keySet()) {
            if (pattern.contains("{") && pathMatcher.isMatch(pattern, path)) {
                return allowedMethods.get(pattern);
            }
        }

        return EnumSet.noneOf(HttpMethod.class);
    }



}
