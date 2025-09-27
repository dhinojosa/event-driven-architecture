package com.evolutionnext.customers.infrastructure.adapter.in;


import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceLoader {
    public static void serveFromResources(HttpExchange exchange, String fileName) throws IOException {
        try (InputStream is = ResourceLoader.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                exchange.sendResponseHeaders(404, 0);
                return;
            }
            byte[] response = is.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}
