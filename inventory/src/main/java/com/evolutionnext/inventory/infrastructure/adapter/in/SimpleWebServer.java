package com.evolutionnext.inventory.infrastructure.adapter.in;

import com.evolutionnext.inventory.application.command.ProductCommand;
import com.evolutionnext.inventory.application.result.ProductResult;
import com.evolutionnext.inventory.domain.aggregate.ProductId;
import com.evolutionnext.inventory.port.in.PublicProductCommandPort;
import com.evolutionnext.orders.customer.web.FormParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.Map;

import static com.evolutionnext.orders.customer.web.ResourceLoader.serveFromResources;

public class SimpleWebServer {
    private final PublicProductCommandPort publicProductCommandPort;
    private HttpServer server;

    public SimpleWebServer(
        PublicProductCommandPort publicProductCommandPort) {
        this.publicProductCommandPort = publicProductCommandPort;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleIndexRequest);
        server.createContext("/product", this::handleCustomerRequest);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleIndexRequest(HttpExchange exchange) throws IOException {
        serveFromResources(exchange, "index.html");
        exchange.close();
    }

    private void handleCustomerRequest(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            serveFromResources(exchange, "create.html");
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> params = FormParser.getFieldData(exchange);
            if (params == null) return;
            String name = params.get("name");
            String description = params.get("description");
            String price = params.get("price");
            String stock = params.get("stock");

            if (name == null || description == null || price == null || stock == null) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }

            ProductCommand productCommand =
                new ProductCommand.Create(name, description,
                    new BigDecimal(price), Integer.parseInt(stock));
            ProductResult productResult = publicProductCommandPort.submit(productCommand);
            System.out.println(productResult);

            String message = String.format("Product %s Successfully Created", name);
            exchange.getResponseHeaders().set("Location", "/?message=" + message.replace(" ", "%20"));
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        }
    }
}
