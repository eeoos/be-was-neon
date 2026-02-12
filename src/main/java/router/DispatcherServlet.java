package router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.handler.HttpRequestHandler;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;

import java.io.IOException;

public class DispatcherServlet {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private final Router router;
    private DispatcherServlet() {
        this.router = new Router();
    }

    private static class DispatcherServletHolder{
        private static final DispatcherServlet INSTANCE = new DispatcherServlet();
    }

    public static DispatcherServlet getInstance() {
        return DispatcherServletHolder.INSTANCE;
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        try {
            logger.debug("요청 처리 시작: {} {}", request.getMethod(), request.getPath());

            HttpRequestHandler handler = router.resolveHandler(request);
            handler.handle(request, response);

            logger.debug("요청 처리 종료: {} {}", request.getMethod(), request.getPath());
        } catch (Exception e) {
            logger.error("요청 처리 중 오류 발생: {}", e.getMessage(), e);
            try {
                response.send500();
            } catch (IOException ex) {
                logger.error("요청 응답 전송 실패", ex);
            }
        }
    }
}
