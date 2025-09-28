package com.evolutionnext.web;


import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FormParser {
    public static Map<String, String> getFieldData(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        Map<String, String> params = new HashMap<>();

        // Parse query string if present
        if (query != null && !query.isEmpty()) {
            parseParamsIntoMap(query, params);
        }

        // Parse body for POST/PUT when form-encoded
        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))
                && contentType != null
                && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            String body = readRequestBody(exchange, resolveCharset(contentType));
            if (body != null && !body.isEmpty()) {
                parseParamsIntoMap(body, params);
            }
        }

        if (params.isEmpty()) {
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
            return null;
        }

        return params;
    }

    private static void parseParamsIntoMap(String paramString, Map<String, String> out) throws IOException {
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String value = kv.length > 1 ? urlDecode(kv[1]) : "";
            if (key != null && !key.isEmpty()) {
                out.put(key, value);
            }
        }
    }

    private static String urlDecode(String s) throws IOException {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String readRequestBody(HttpExchange exchange, Charset charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = exchange.getRequestBody()) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
                sb.append(new String(buf, 0, read, charset));
            }
        }
        return sb.toString();
    }

    private static Charset resolveCharset(String contentType) {
        try {
            for (String part : contentType.split(";")) {
                String p = part.trim().toLowerCase();
                if (p.startsWith("charset=")) {
                    String cs = p.substring("charset=".length()).trim();
                    return Charset.forName(cs);
                }
            }
        } catch (Exception ignored) { }
        return StandardCharsets.UTF_8;
    }
}
