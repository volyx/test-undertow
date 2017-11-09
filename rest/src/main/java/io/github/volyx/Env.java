package io.github.volyx;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.VkontakteApi;
import io.github.volyx.handlers.context.UndertowSessionStore;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oauth.profile.vk.VkProfileDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Env {
    LOCAL("local"), DEV("dev"), PROD("prod");

    private final String name;
    Env(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // {{start:logger}}
    private static final Logger logger = LoggerFactory.getLogger(Env.class);
    private static final Env currentEnv;
    private static final Config config;
    static {
        String env = "local";
        if (Configs.system().hasPath("env")) {
            env = Configs.system().getString("env");
        }
        currentEnv = Env.valueOf(env.toUpperCase());
        logger.info("Current Env: {}", currentEnv.getName());

        OAuth20Configuration auth20Configuration = new OAuth20Configuration();
        auth20Configuration.setApi(VkontakteApi.instance());
        auth20Configuration.setProfileDefinition(new VkProfileDefinition());
        auth20Configuration.setScope("user");
        auth20Configuration.setKey("62374f5573a89a8f9900");
        auth20Configuration.setSecret("01dd26d60447677ceb7399fb4c744f545bb86359");
        OAuth20Client client = new OAuth20Client();
        client.setConfiguration(auth20Configuration);
        client.setCallbackUrl("http://localhost:8080/callback");

        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator());
        Clients clients = new Clients("http://localhost:8080/callback", client, parameterClient);

        config = new Config(clients);
    }

    public static Env get() {
        return currentEnv;
    }

    public static Config getConfig() {
        return config;
    }

    public static void main(String[] args) {
        Env env = currentEnv.get();
    }
    // {{end:logger}}
}