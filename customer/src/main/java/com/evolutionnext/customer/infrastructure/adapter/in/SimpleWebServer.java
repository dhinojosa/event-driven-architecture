package com.evolutionnext.customer.infrastructure.adapter.in;

import com.evolutionnext.customer.application.command.CustomerCommand;
import com.evolutionnext.customer.port.in.PublicCustomerCommandPort;
import com.evolutionnext.orders.customer.web.FormParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import static com.evolutionnext.orders.customer.web.ResourceLoader.serveFromResources;

public class SimpleWebServer {
    private final PublicCustomerCommandPort publicCustomerCommandPort;
    private HttpServer server;

    public SimpleWebServer(
        PublicCustomerCommandPort publicCustomerCommandPort) {
        this.publicCustomerCommandPort = publicCustomerCommandPort;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
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
        serveFromResources(exchange, "index.html");
        exchange.close();
    }

    private void handleCustomerRequest(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            serveFromResources(exchange, "index.html");
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
            publicCustomerCommandPort.submit(customerCommand);
            String message = String.format("Customer %s %s Successfully Created", firstName, lastName);
            exchange.getResponseHeaders().set("Location", "/?message=" + message.replace(" ", "%20"));
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        }
    }
}
