package ru.netology.server;

import ru.netology.logger.ConsoleLogger;
import ru.netology.logger.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
  private final Logger logger = new ConsoleLogger<>(Server.class);
  private final int maxThreadsNumber = 64;
  private final List<String> validPaths = new ArrayList<>();
  private final ExecutorService executorService = Executors.newFixedThreadPool(maxThreadsNumber);

  public Server() {
    logger.info("Server started");
  }

  public void addEndpoint(String endpoint) {
    this.validPaths.add(endpoint);
  }

  public void addEndpoint(List<String> endpoint) {
    this.validPaths.addAll(endpoint);
  }

  public void listen(int port) {
    try (final var serverSocket = new ServerSocket(port)) {
      while (true) {
        listen(serverSocket.accept());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void listen(Socket socket) {
    executorService.submit(() -> {
      try {
        new SocketProcessor(socket, this).processRequest();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public Boolean checkForProperlyEndpoint(String path, BufferedOutputStream out) throws IOException {
    if (!validPaths.contains(path)) {
      out.write((
          "HTTP/1.1 404 Not Found\r\n" +
              "Content-Length: 0\r\n" +
              "Connection: close\r\n" +
              "\r\n"
      ).getBytes());
      out.flush();
      logger.info("Unknown request");
      return false;
    }
    return true;
  }
}


