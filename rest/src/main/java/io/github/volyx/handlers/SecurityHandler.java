package io.github.volyx.handlers;


import io.github.volyx.handlers.context.UndertowSessionStore;
import io.github.volyx.handlers.context.UndertowWebContext;
import io.github.volyx.handlers.security.UndertowProfileManager;
import io.github.volyx.handlers.security.http.UndertowNopHttpActionAdapter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.engine.SecurityLogic;

import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 * <p>This handler protects an url, based on the {@link #securityLogic}.</p>
 * <p>
 * <p>The configuration can be provided via the following parameters: <code>config</code> (account configuration),
 * <code>clients</code> (list of clients for authentication), <code>authorizers</code> (list of authorizers),
 * <code>matchers</code> (list of matchers) and <code>multiProfile</code> (whether multiple profiles should be kept).</p>
 *
 * @author Jerome Leleu
 * @since 1.2.0
 */
public class SecurityHandler implements HttpHandler {

    private SecurityLogic<Object, UndertowWebContext> securityLogic;

    private HttpHandler toWrap;

    private Config config;

    private String clients;

    private String authorizers;

    private String matchers;

    private Boolean multiProfile;
    private InMemorySessionManager sessionManager;
    private SessionCookieConfig cookieConfig;

    protected SecurityHandler(final HttpHandler toWrap, final Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile) {
        securityLogic = new DefaultSecurityLogic<Object, UndertowWebContext>();
        ((DefaultSecurityLogic<Object, UndertowWebContext>) securityLogic).setProfileManagerFactory(UndertowProfileManager::new);
        this.toWrap = toWrap;
        this.config = config;
        this.clients = clients;
        this.authorizers = authorizers;
        this.matchers = matchers;
        this.multiProfile = multiProfile;
        sessionManager = new InMemorySessionManager("123");
        cookieConfig = new SessionCookieConfig();

    }

    public static HttpHandler build(final HttpHandler toWrap, Config config) {
        return build(toWrap, config, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients) {
        return build(toWrap, config, clients, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers) {
        return build(toWrap, config, clients, authorizers, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers) {
        return build(toWrap, config, clients, authorizers, matchers, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile) {
        return build(toWrap, config, clients, authorizers, matchers, multiProfile, null);
    }

    public static HttpHandler build(final HttpHandler toWrap, Config config, final String clients, final String authorizers, final String matchers, final Boolean multiProfile, final SecurityLogic<Object, UndertowWebContext> securityLogic) {
        final SecurityHandler securityHandler = new SecurityHandler(toWrap, config, clients, authorizers, matchers, multiProfile);
        if (securityLogic != null) {
            securityHandler.setSecurityLogic(securityLogic);
        }
        return new BlockingHandler(securityHandler);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        assertNotNull("securityLogic", securityLogic);
        assertNotNull("config", config);
//        final UndertowWebContext context = new UndertowWebContext(exchange, config.getSessionStore());
        exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
        exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, cookieConfig);
        final UndertowWebContext context = new UndertowWebContext(exchange, new UndertowSessionStore(exchange));

        securityLogic.perform(context, this.config, (ctx, parameters) -> {

            toWrap.handleRequest(exchange);
            return null;

        }, UndertowNopHttpActionAdapter.INSTANCE, this.clients, this.authorizers, this.matchers, this.multiProfile);
    }

    protected SecurityLogic<Object, UndertowWebContext> getSecurityLogic() {
        return securityLogic;
    }

    protected void setSecurityLogic(final SecurityLogic<Object, UndertowWebContext> securityLogic) {
        this.securityLogic = securityLogic;
    }
}
