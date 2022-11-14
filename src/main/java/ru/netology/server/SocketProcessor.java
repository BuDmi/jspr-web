package ru.netology.server;

import ru.netology.Request;
import ru.netology.logger.ConsoleLogger;
import ru.netology.logger.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class SocketProcessor {
    private final Logger logger = new ConsoleLogger<>(SocketProcessor.class);
    private final Server server;
    private final BufferedInputStream in;
    private final BufferedOutputStream out;
    private final Socket socket;

    public SocketProcessor(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new BufferedInputStream(socket.getInputStream());
            this.out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest() throws IOException {

        Request request = new Request().parse(in);

        if (request == null) {
            badRequest(out);
            return;
        }

        if (request.getQueryParams() != null) {
            logger.info("Get param age = " + request.getQueryParam("age"));
            logger.info("Get params: " + request.getQueryParams());
        }

        if (request.getPostParams() != null) {
            logger.info("Post param age = " + request.getPostParam("age"));
            logger.info("Post params: " + request.getPostParams());
        }

        logger.info("Start processing request: " + request.getRequestMethod());
        if (!server.checkForProperlyEndpoint(request, out)) {
            socket.close();
            return;
        }
        server.getHandler(request).handle(request, out);
        logger.info("Finish processing request: " + request.getRequestMethod());
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
            "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n"
        ).getBytes());
        out.flush();
    }
}
