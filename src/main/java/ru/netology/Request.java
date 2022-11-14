package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private String requestMethod;
    private String path;
    private List<NameValuePair> params;
    private List<String> headers;
    private String body;

    public Request parse(String[] requestParts) {
        this.requestMethod = requestParts[0];
        parsePathAndParams(requestParts[1]);
        return this;
    }
    private void parsePathAndParams(String requestPart) {
        int index = requestPart.indexOf("?");
        this.path = requestPart.substring(0, index);
        this.params = URLEncodedUtils.parse(requestPart.substring(index + 1), Charset.defaultCharset());
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        var filteredParams = params.stream().filter(param -> param.getName().equals(name))
            .collect(Collectors.toList());
        if (filteredParams.isEmpty()) {
            return "";
        } else {
            return filteredParams.get(0).getValue();
        }
    }
    public List<NameValuePair> getQueryParams() {
        return params;
    }
}
