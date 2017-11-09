package io.github.volyx.handlers.security.http;

import io.github.volyx.Server;
import io.github.volyx.handlers.context.UndertowWebContext;
import org.pac4j.core.http.HttpActionAdapter;

/**
 * No-operation HTTP action adapter for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class UndertowNopHttpActionAdapter implements HttpActionAdapter<Object, UndertowWebContext> {

    public static final UndertowNopHttpActionAdapter INSTANCE = new UndertowNopHttpActionAdapter();

    @Override
    public Object adapt(int code, UndertowWebContext context) {
        if (code == 401) {
            Server.unauthorized(context.getExchange());
        }
        return null;
    }
}