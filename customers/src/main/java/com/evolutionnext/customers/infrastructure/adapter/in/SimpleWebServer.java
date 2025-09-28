package com.evolutionnext.customers.infrastructure.adapter.in;

import com.evolutionnext.customers.application.command.CustomerCommand;
import com.evolutionnext.customers.application.result.CustomerResult;
import com.evolutionnext.customers.port.in.PublicCustomerCommandPort;
import com.evolutionnext.web.FormParser;
import com.evolutionnext.web.ResourceLoader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class SimpleWebServer {
    private final PublicCustomerCommandPort publicCustomerCommandPort;
    private HttpServer server;

    public SimpleWebServer(
        PublicCustomerCommandPort publicCustomerCommandPort) {
        this.publicCustomerCommandPort = publicCustomerCommandPort;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", this::handleIndexRequest);
        server.createContext("/customer", this::handleCustomerRequest);
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleIndexRequest(HttpExchange exchange) throws IOException {
        ResourceLoader.serveFromResources(exchange, "index.html");
        exchange.close();
    }

    private void handleCustomerRequest(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            ResourceLoader.serveFromResources(exchange, "create.html");
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> params = FormParser.getFieldData(exchange);
            if (params == null) return;
            String firstName = params.get("firstName");
            String lastName = params.get("lastName");
            String email = params.get("email");
            String state = params.get("state");

            if (firstName == null || lastName == null || email == null || state == null) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }

            CustomerCommand customerCommand = new CustomerCommand.Create(firstName, lastName, email, state);
            CustomerResult customerResult = publicCustomerCommandPort.submit(customerCommand);
            System.out.println(customerResult);
        }
    }
}
