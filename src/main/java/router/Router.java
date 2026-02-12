package router;

import router.handler.HttpRequestHandler;
import router.handler.impl.HtmlFileHandler;
import router.handler.impl.MethodNotAllowHandler;
import router.handler.impl.StaticFileHandler;
import webserver.common.ContentType;
import webserver.http.request.HttpRequest;

public class Router {

    private final RouteRegistry routeRegistry;
    private final HttpRequestHandler staticFileHandler = new StaticFileHandler();
    private final HttpRequestHandler htmlFileHandler = new HtmlFileHandler();

    public Router() {
        this.routeRegistry = RouteRegistry.getInstance();
    }

    public HttpRequestHandler resolveHandler(HttpRequest request) {
        String path = request.getPath();
        String method = request.getMethod();
        
        if (routeRegistry.hasRoutesForPath(path)) {
            if (!routeRegistry.isMethodAllowed(method, path)) {
                return new MethodNotAllowHandler(routeRegistry.getAllowedMethods(path));
            }

            HttpRequestHandler handler = routeRegistry.findHandler(method, path);
            if (handler != null) {
                return handler;
            }
        }
        ContentType contentType = ContentType.getContentTypeByPath(path);
        return contentType == ContentType.HTML ? htmlFileHandler : staticFileHandler;
    }
}


