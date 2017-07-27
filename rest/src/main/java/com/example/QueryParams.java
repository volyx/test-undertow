package com.example;


import io.undertow.server.HttpServerExchange;

import java.util.Deque;
import java.util.Optional;

public interface QueryParams {

    default Optional<String> queryParam(HttpServerExchange exchange, String name) {
        return Optional.ofNullable(exchange.getQueryParameters().get(name))
                .map(Deque::getFirst);
    }

    default Optional<Long> queryParamAsLong(HttpServerExchange exchange, String name) {
        return queryParam(exchange, name).map(Long::parseLong);
    }

    default Optional<Integer> queryParamAsInteger(HttpServerExchange exchange, String name) {
        return queryParam(exchange, name).map(Integer::parseInt);
    }

    default Optional<Boolean> queryParamAsBoolean(HttpServerExchange exchange, String name) {
        return queryParam(exchange, name).map(Boolean::parseBoolean);
    }
}