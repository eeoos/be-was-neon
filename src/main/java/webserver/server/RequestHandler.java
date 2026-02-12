package webserver.server;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.DispatcherServlet;
import webserver.http.request.HttpRequest;
import webserver.http.request.HttpRequestParser;
import webserver.http.response.HttpResponse;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = HttpRequestParser.parse(in);

            DataOutputStream dos = new DataOutputStream(out);
            HttpResponse response = new HttpResponse(dos);

            DispatcherServlet.getInstance().dispatch(request, response);

        } catch (IllegalArgumentException | IOException e) {
            logger.error(e.getMessage());
        }finally {
            try {
                connection.close();
            } catch (IOException e) {
                logger.error("Connection 종료 실패", e);
            }
        }
    }
}
