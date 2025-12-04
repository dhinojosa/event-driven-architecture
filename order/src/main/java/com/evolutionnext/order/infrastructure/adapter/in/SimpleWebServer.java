package com.evolutionnext.order.infrastructure.adapter.in;


import com.evolutionnext.order.port.in.PublicCustomerQueryPort;
import com.evolutionnext.order.port.in.PublicOrderCommandPort;
import com.evolutionnext.order.port.in.PublicProductQueryPort;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleWebServer {
    private final IndexHandler indexHandler;
    private final ProductHandler productHandler;
    private final CustomerHandler customerHandler;

    private HttpServer server;

    public SimpleWebServer(IndexHandler indexHandler,
                           ProductHandler productHandler,
                           CustomerHandler customerHandler) {
        this.indexHandler = indexHandler;
        this.productHandler = productHandler;
        this.customerHandler = customerHandler;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", indexHandler);
        server.createContext("/api/customer", customerHandler);
        server.createContext("/api/product", productHandler);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

}
