package ru.netology.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger<T> implements Logger {
    private final Class<T> clazz;
    public ConsoleLogger(Class<T> clazz) {
        this.clazz = clazz;
    }
    @Override
    public void info(String msg) {
        var time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        System.out.printf("%23s INFO --- %-40s : %-80s\n", time, clazz.getName(), msg);
    }
}
