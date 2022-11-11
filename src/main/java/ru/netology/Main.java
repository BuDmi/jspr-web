package ru.netology;

import ru.netology.server.Server;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        final var server = new Server();

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            var message = "Hello! GET!";
            try {
                responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + "text/plain" + "\r\n" +
                        "Content-Length: " + message.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" + message
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            var message = "Hello! POST!";
            try {
                responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + "text/plain" + "\r\n" +
                        "Content-Length: " + message.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" + message
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        server.listen(9999);
    }
}
