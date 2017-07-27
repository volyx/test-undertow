package com.example;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.google.common.collect.Maps;

import java.util.Map;

public class Response {
    private Map<String, Object> data;

    private Response() {
        super();
        this.data = null;
    }

    public static Response create() {
        return new Response();
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    public Response with(String key, Object value) {
        if (null == data) {
            data = Maps.newLinkedHashMap();
        }
        if (data.containsKey(key)) {
            throw new IllegalArgumentException(key + " already has data");
        }
        data.put(key, value);
        return this;
    }
}