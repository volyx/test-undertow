package io.github.volyx.handlers.security;


import io.github.volyx.handlers.context.UndertowWebContext;
import io.github.volyx.handlers.security.util.UndertowHelper;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import java.util.LinkedHashMap;

/**
 * Specific profile manager for Undertow.
 *
 * @author Jerome Leleu
 * @since 1.2.1
 */
public class UndertowProfileManager extends ProfileManager<CommonProfile> {

    public UndertowProfileManager(final WebContext context) {
        super(context);
    }

    protected LinkedHashMap<String, CommonProfile> retrieveAll(boolean readFromSession) {

        final LinkedHashMap<String, CommonProfile> profiles = super.retrieveAll(readFromSession);
        UndertowHelper.populateContext((UndertowWebContext) context, profiles);
        return profiles;
    }
}