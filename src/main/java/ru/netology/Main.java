package ru.netology;

import ru.netology.server.Server;

import java.util.List;

public class Main {
    public static void main(String[] args){
        var endpoints = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        final var server = new Server();
        server.addEndpoint(endpoints);

        server.listen(9999);
    }
}
