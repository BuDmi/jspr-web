package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {
    private String requestMethod;
    private String path;
    private List<NameValuePair> getParams;
    private List<NameValuePair> postParams;
    private List<String> headers;
    private String body;

    private final List<String> allowedMethods = List.of("GET", "POST");

    public Request parse(BufferedInputStream in) throws IOException {
        final var limit = 4096;

        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        this.requestMethod = requestLine[0];
        if (!allowedMethods.contains(requestMethod)) {
            return null;
        }

        parsePathAndParams(requestLine[1]);

        if (!path.startsWith("/")) {
            return null;
        }

        // ищем заголовки
        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        this.headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        final var contentTypeHeader = extractHeader(headers, "Content-Type");
        if (!requestMethod.equals("GET")) {
            in.skip(headersDelimiter.length);

            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                if (contentTypeHeader.isPresent() && contentTypeHeader.get().equals("application/x-www-form-urlencoded")) {
                    this.body = new String(bodyBytes);

                    this.postParams = URLEncodedUtils.parse(body, Charset.defaultCharset());
                }
            }
        }

        return this;
    }
    private void parsePathAndParams(String requestPart) {
        int index = requestPart.indexOf("?");
        if (index > 0) {
            this.path = requestPart.substring(0, index);
            this.getParams = URLEncodedUtils.parse(requestPart.substring(index + 1), Charset.defaultCharset());
        } else {
            this.path = requestPart;
        }
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
            .filter(o -> o.startsWith(header))
            .map(o -> o.substring(o.indexOf(" ")))
            .map(String::trim)
            .findFirst();
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        var filteredParams = getParams.stream().filter(param -> param.getName().equals(name))
            .collect(Collectors.toList());
        if (filteredParams.isEmpty()) {
            return "";
        } else {
            return filteredParams.get(0).getValue();
        }
    }
    public List<NameValuePair> getQueryParams() {
        return getParams;
    }

    public String getPostParam(String name) {
        var filteredParams = postParams.stream().filter(param -> param.getName().equals(name))
            .collect(Collectors.toList());
        if (filteredParams.isEmpty()) {
            return "";
        } else {
            return filteredParams.get(0).getValue();
        }
    }
    public List<NameValuePair> getPostParams() {
        return postParams;
    }
}
