package io.github.volyx;

import io.github.volyx.data.providers.http.StationService;
import io.github.volyx.notification.INotificationService;
import io.github.volyx.notification.NotificationService;
import io.github.volyx.notification.TelegramNotificationService;
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

import static io.github.volyx.CustomHandlers.timed;

public class WebpackServer {
    private static final Logger log = LoggerFactory.getLogger(WebpackServer.class);

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
                .with("filters", Injector.get(Options.class).filters)
                .with("trains", Injector.get(INotificationService.class).getTrains())
                .with("logs", Injector.get(INotificationService.class).getLogs())
                .with("name", name);
        Exchange.body().sendHtmlTemplate(exchange, "static/templates/src/hello", response);
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
                .addExceptionHandler(Throwable.class, WebpackServer::error);
    }

    // Useful middleware
    private static HttpHandler wrapWithMiddleware(HttpHandler handler) {
        return MiddlewareBuilder.begin(BlockingHandler::new)
                .next(CustomHandlers::gzip)
                .next(ex -> CustomHandlers.accessLog(ex, log))
                .next(CustomHandlers::statusCodeMetrics)
                .next(WebpackServer::exceptionHandler)
                .complete(handler);
    }

    // Simple routing, anything not matching a route will fall back
    // to the not found handler.
    private static final HttpHandler ROUTES = new RoutingHandler()
            .get("/", timed("home", WebpackServer::home))
            .get("/hello", timed("hello", WebpackServer::hello))
            .get("/static*", timed("static", CustomHandlers.resource("")))
            .setFallbackHandler(timed("notfound", WebpackServer::notFound))
            ;

    public static void main(String[] args) throws ParseException, IOException {

        Options opts = Options.parse(args);
        StationService stationService = new StationService();
        INotificationService notificationService = new TelegramNotificationService();
        if (opts.isValid) {
            TimerTask instance = new ScheduleTask(opts.filters, stationService, notificationService);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(instance, 0, opts.timeout);
        }

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
        Injector.put(INotificationService.class, notificationService);
        Injector.put(StationService.class, stationService);


        SimpleServer server = SimpleServer.simpleServer(wrapWithMiddleware(ROUTES));
        server.start();
    }
    // {{end:server}}
}