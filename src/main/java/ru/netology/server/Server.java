package ru.netology.server;

import ru.netology.Handler;
import ru.netology.Request;
import ru.netology.logger.ConsoleLogger;
import ru.netology.logger.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
  private final Logger logger = new ConsoleLogger<>(Server.class);
  private final int maxThreadsNumber = 64;
  private final ExecutorService executorService = Executors.newFixedThreadPool(maxThreadsNumber);
  private final Map<String, Handler> concurrentHashMapForRequest = new ConcurrentHashMap<>();

  public Server() {
    logger.info("Server started");
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

  public Boolean checkForProperlyEndpoint(Request request, BufferedOutputStream out) throws IOException {
    if (!concurrentHashMapForRequest.containsKey(request.getRequestMethod() + request.getPath())) {
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

  public void addHandler(String requestMethod, String path, Handler handler) {
    concurrentHashMapForRequest.put(requestMethod + path, handler);
  }

  public Handler getHandler(Request request) {
    return concurrentHashMapForRequest.get(request.getRequestMethod() + request.getPath());
  }
}


