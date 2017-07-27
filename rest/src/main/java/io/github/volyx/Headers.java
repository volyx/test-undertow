package io.github.volyx;


import io.undertow.attribute.RequestHeaderAttribute;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

import java.util.Optional;

public interface Headers {

    default Optional<String> getHeader(HttpServerExchange exchange, HttpString header) {
        RequestHeaderAttribute reqHeader = new RequestHeaderAttribute(header);
        return Optional.ofNullable(reqHeader.readAttribute(exchange));
    }

    default Optional<String> getHeader(HttpServerExchange exchange, String header) {
        RequestHeaderAttribute reqHeader = new RequestHeaderAttribute(new HttpString(header));
        return Optional.ofNullable(reqHeader.readAttribute(exchange));
    }
}
