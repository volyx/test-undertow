package io.github.volyx;

import com.codahale.metrics.health.HealthCheck.Result;
import io.undertow.Handlers;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class CustomHandlers {
    private static final Logger log = LoggerFactory.getLogger(CustomHandlers.class);

    public static AccessLogHandler accessLog(HttpHandler next, Logger logger) {
        return new AccessLogHandler(next, new Slf4jAccessLogReceiver(logger), "combined", CustomHandlers.class.getClassLoader());
    }

    public static AccessLogHandler accessLog(HttpHandler next) {
        final Logger logger = LoggerFactory.getLogger("com.stubbornjava.accesslog");
        return accessLog(next, logger);
    }

    public static HttpHandler gzip(HttpHandler next) {
        return new EncodingHandler(new ContentEncodingRepository()
                .addEncodingHandler("gzip",
                        // This 1000 is a priority, not exactly sure what it does.
                        new GzipEncodingProvider(), 1000,
                        // Anything under a content-length of 20 will not be gzipped
                        Predicates.parse("max-content-size(20)")))
                .setNext(next);
    }

    public static HttpHandler resource(String prefix) {
        ResourceManager resourceManager = null;
        if (Env.LOCAL == Env.get()) {
            String path = AssetsConfig.assetsRoot() + "/" + prefix;
            log.info("using local file resource manager {}", path);
            resourceManager = new FileResourceManager(new File(path), 1024 * 1024);
        } else {
            log.info("using classpath file resource manager");
            resourceManager = new ClassPathResourceManager(CustomHandlers.class.getClassLoader(), AssetsConfig.assetsRoot());
        }
        ResourceHandler handler = new ResourceHandler(resourceManager);
        handler.setCacheTime((int)TimeUnit.HOURS.toSeconds(4));
        return handler;
    }

    public static StatusCodeHandler statusCodeMetrics(HttpHandler next) {
        return new StatusCodeHandler(next, "status.code");
    }

    public static TimingHttpHandler timed(String name, HttpHandler next) {
        return new TimingHttpHandler(next, name);
    }

    public static void metrics(HttpServerExchange exchange) {
        Exchange.body().sendJson(exchange, Metrics.registry());
    }

    // {{start:health}}
    public static void health(HttpServerExchange exchange) {
        SortedMap<String, Result> results = HealthChecks.getHealthCheckRegistry().runHealthChecks();
        boolean unhealthy = results.values().stream().anyMatch(result -> !result.isHealthy());

        if (unhealthy) {
            /*
             *  Set a 500 status code also. A lot of systems / dev ops tools can
             *  easily test status codes but are not set up to parse JSON.
             *  Let's keep it simple for everyone.
             */
            exchange.setStatusCode(500);
        }
        Exchange.body().sendJson(exchange, results);
    }
    // {{end:health}}

    public static ExceptionHandler exception(HttpHandler handler) {
        return Handlers.exceptionHandler((HttpServerExchange exchange) -> {
            try {
                handler.handleRequest(exchange);
            } catch (Throwable th) {
                log.error("exception thrown at " + exchange.getRequestURI(), th);
                throw th;
            }
        });
    }
}