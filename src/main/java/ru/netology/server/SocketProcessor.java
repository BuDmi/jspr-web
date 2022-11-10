package ru.netology.server;

import ru.netology.Request;
import ru.netology.logger.ConsoleLogger;
import ru.netology.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketProcessor {
    private final Logger logger = new ConsoleLogger<>(SocketProcessor.class);
    private final Server server;
    private final BufferedReader in;
    private final BufferedOutputStream out;
    private final Socket socket;

    public SocketProcessor(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequest() throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length < 2) {
            socket.close();
            return;
        }

        Request request = new Request().parse(parts);

        logger.info("Start processing request: " + request.getRequestMethod());
        if (!server.checkForProperlyEndpoint(request, out)) {
            socket.close();
            return;
        }
        server.getHandler(request).handle(request, out);
        logger.info("Finish processing request: " + request.getRequestMethod());
    }
}
