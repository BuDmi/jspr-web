package ru.netology.server;

import ru.netology.logger.ConsoleLogger;
import ru.netology.logger.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

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

        if (parts.length != 3) {
            socket.close();
            return;
        }

        final var path = parts[1];
        logger.info("Start processing request: " + path);
        if (!server.checkForProperlyEndpoint(path, out)) return;

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            sendFileWithProcessedContent(out, filePath, mimeType);
            logger.info("Finish processing request: " + path);
            return;
        }

        sendOriginFile(out, filePath, mimeType);
        logger.info("Finish processing request: " + path);
    }

    private void sendFileWithProcessedContent(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
            "{time}",
            LocalDateTime.now().toString()
        ).getBytes();
        out.write((
            "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private void sendOriginFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        out.write((
            "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
