package io.github.volyx;

import com.github.scribejava.apis.VkontakteApi;
import io.github.volyx.handlers.SecurityHandler;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.vk.VkProfileDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;

import java.io.IOException;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

import static io.github.volyx.CustomHandlers.timed;

public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    // {{start:routes}}
    // Simple not found 404 page
    public static void notFound(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/notfound", Response.create());
    }

    // Default error page when something unexpected happens
    public static void error(HttpServerExchange exchange) {
        exchange.setStatusCode(500);
        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/serverError", Response.create());
    }

    // Render homepage
    public static void home(HttpServerExchange exchange) {
        exception(exchange);
        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/home", Response.create());
    }

    // Render hello {name} page based on the name query param.
    public static void hello(HttpServerExchange exchange) {
        exception(exchange);

        String name = Exchange.queryParams()
                .queryParam(exchange, "name")
                .filter(s -> !Strings.isNullOrEmpty(s))
                .orElse("world");
        Response response = Response.create()
//                .with("filters", Injector.get(Options.class).filters)
//                .with("trains", Injector.get(INotificationService.class).getTrains())
//                .with("logs", Injector.get(INotificationService.class).getLogs())
                .with("name", name);
        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/hello", response);
    }

    // / Render hello {name} page based on the name query param.
    public static void unauthorized(HttpServerExchange exchange) {
        exception(exchange);

        String name = Exchange.queryParams()
                .queryParam(exchange, "name")
                .filter(s -> !Strings.isNullOrEmpty(s))
                .orElse("world");
        Response response = Response.create()
//                .with("filters", Injector.get(Options.class).filters)
//                .with("trains", Injector.get(INotificationService.class).getTrains())
//                .with("logs", Injector.get(INotificationService.class).getLogs())
                .with("title", "unauthorized")
                .with("name", name);

        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/unauthorized", response);
    }

    // Helper function to forcibly throw an exception whenever the query
    // parameter exception=true
    private static void exception(HttpServerExchange exchange) {
        if (Exchange.queryParams().queryParamAsBoolean(exchange, "exception").orElse(false)) {
            throw new RuntimeException("Poorly Named Exception!!!");
        }
    }
    // {{end:routes}}

    // {{start:server}}
    // We are currently handling all exceptions the same way
    private static HttpHandler exceptionHandler(HttpHandler next) {
        return CustomHandlers.exception(next)
                .addExceptionHandler(Throwable.class, Server::error);
    }

    // Useful middleware
    private static HttpHandler wrapWithMiddleware(HttpHandler handler) {
        return MiddlewareBuilder.begin(BlockingHandler::new)
                .next(CustomHandlers::gzip)
                .next(ex -> CustomHandlers.accessLog(ex, log))
                .next(CustomHandlers::statusCodeMetrics)
                .next(Server::exceptionHandler)
                .complete(handler);
    }


    // Simple routing, anything not matching a route will fall back
    // to the not found handler.
    private static final HttpHandler ROUTES = new RoutingHandler()
            .get("/", timed("home", Server::home))
            .get("/hello", timed("hello", Server::hello))

            .get("/private", timed("private",  SecurityHandler.build(new HttpHandler() {
                @Override
                public void handleRequest(HttpServerExchange exchange) throws Exception {
                    exception(exchange);

                    String name = Exchange.queryParams()
                            .queryParam(exchange, "name")
                            .filter(s -> !Strings.isNullOrEmpty(s))
                            .orElse("world");
                    Response response = Response.create()
                            .with("name", name);
                    Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/hello", response);
                }
            }, Env.getConfig(), "VkClients")))
            .get("/static*", timed("static", CustomHandlers.resource("")))
            .setFallbackHandler(timed("notfound", Server::notFound));

    public static void main(String[] args) throws ParseException, IOException {

        Options opts = Options.parse(args);
//        if (opts.isValid) {
//            TimerTask instance = new ScheduleTask(opts.filters, stationService, notificationService);
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(instance, 0, opts.timeout);
//        }

//
//        get("/admin", (req, res) -> {
//            Map<String, Object> model = new HashMap<>();
//            model.put("filters", opts.filters);
//            model.put("trains", notificationService.getTrains());
//            return render(model, "/admin.hbs");
//        });
//
//        Gson gson = new Gson();
//
//        get("/api/station", (req, res) -> {
//            final String term = req.queryMap().get("term").value();
//            final List<String> stations = stationService.getStationsName(term);
//            return gson.toJson(stations);
//        });



        Injector.put(Options.class, opts);


        SimpleServer server = SimpleServer.simpleServer(wrapWithMiddleware(ROUTES));
        server.start();
    }
    // {{end:server}}
}