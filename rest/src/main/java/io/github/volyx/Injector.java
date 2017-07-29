package io.github.volyx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Injector {

    private static Map<Class, Object> services = new ConcurrentHashMap<>();

    public static <T> T get(Class<T> clazz) {
        return (T) services.get(clazz);
    }

    public static void put(Class clazz, Object object) {
        services.put(clazz, object);
    }
}
