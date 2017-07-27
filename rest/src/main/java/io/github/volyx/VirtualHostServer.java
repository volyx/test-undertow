package io.github.volyx;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.NameVirtualHostHandler;
import okhttp3.HttpUrl;

public class VirtualHostServer {

    // Simple hello world
    public static void hello(HttpServerExchange exchange) {
        Exchange.body().sendText(exchange, "Hello World");
    }

    /*
     * Using our utility to get the current url redirect
     * to the given host keeping paths and query strings.
     */
    public static HttpHandler redirectToHost(String host) {
        return exchange -> {
            HttpUrl url = Exchange.urls().currentUrl(exchange);
            Exchange.redirect().permanent(exchange, url.newBuilder().host(host).build().toString());
        };
    }

    private static final HttpHandler ROUTES = new RoutingHandler()
            .get("/hello", RedirectServer::hello)
            .setFallbackHandler(RoutingHandlers::notFoundHandler);

    public static void main(String[] args) {
        NameVirtualHostHandler handler = Handlers.virtualHost()
                .addHost("example.com", VirtualHostServer.redirectToHost("www.example.com"))
                .addHost("www.example.com", ROUTES);
        SimpleServer server = SimpleServer.simpleServer(handler);
        server.start();
    }

}
