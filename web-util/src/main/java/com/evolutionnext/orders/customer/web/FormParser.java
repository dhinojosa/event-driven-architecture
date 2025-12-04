package com.evolutionnext.orders.customer.web;


import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormParser {
    private static final Logger logger = LoggerFactory.getLogger(FormParser.class);

    public static Map<String, List<String>> getFieldData(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");


        logger.info("Received {} request for {} with query {}", method, exchange.getRequestURI(), query);
        Map<String, List<String>> params = new HashMap<>();

        if (query != null && !query.isEmpty()) {
            parseParamsIntoMap(query, params);
        }

        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))
                && contentType != null
                && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            String body = readRequestBody(exchange, resolveCharset(contentType));
            logger.info("Request body: {}", body);
            if (!body.isEmpty()) {
                parseParamsIntoMap(body, params);
            }
        }

        if (params.isEmpty()) {
            logger.warn("No parameters found in the request");
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
            return null;
        }

        return params;
    }

    private static void parseParamsIntoMap(String paramString, Map<String, List<String>> out) throws IOException {
        String[] pairs = paramString.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = urlDecode(kv[0]);
            String value = kv.length > 1 ? urlDecode(kv[1]) : "";

            if (key != null && !key.isEmpty()) {
                // CHANGED: Use computeIfAbsent to handle the list creation
                out.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
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
        } catch (Exception e) {
            logger.warn("Failed to resolve charset from content type: {}", contentType, e);
        }
        return StandardCharsets.UTF_8;
    }
}
