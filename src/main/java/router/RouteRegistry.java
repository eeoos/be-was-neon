package router;

import router.handler.HttpRequestHandler;
import router.handler.impl.*;
import webserver.common.HttpMethod;

import java.util.*;

import static webserver.common.HttpMethod.*;

public class RouteRegistry {

    private final Map<HttpMethod, Map<String, HttpRequestHandler>> routes =
            new EnumMap<>(HttpMethod.class);
    private final Map<String, Set<HttpMethod>> allowedMethods = new HashMap<>();

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
        registerRoute(GET, "/article/index.html", new ArticleFormHandler());
    }

    public void registerRoute(HttpMethod method, String path, HttpRequestHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
        allowedMethods.computeIfAbsent(path, k -> EnumSet.noneOf(HttpMethod.class)).add(method);
    }

    public HttpRequestHandler findHandler(String method, String path) {
        try {
            Map<String, HttpRequestHandler> pathMap = routes.get(from(method));
            if (pathMap == null) {
                return null;
            }
            return pathMap.get(path);
        } catch (IllegalArgumentException e) { // HttpMethod에 없는 메서드라면
            return null;
        }
    }

    public boolean isMethodAllowed(String method, String path) {
        try {
            Set<HttpMethod> methods = allowedMethods.get(path);
            return methods != null && methods.contains(from(method));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasRoutesForPath(String path) {
        return allowedMethods.containsKey(path);
    }

    public Set<HttpMethod> getAllowedMethods(String path) {
        return allowedMethods.getOrDefault(path, EnumSet.noneOf(HttpMethod.class));
    }



}
