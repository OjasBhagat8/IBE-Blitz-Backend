package com.example.ibe_blits_backend.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecretsBootstrapConfigTest {

    private static final String URL_KEY = "SPRING_DATASOURCE_URL";
    private static final String USER_KEY = "SPRING_DATASOURCE_USERNAME";
    private static final String PASS_KEY = "SPRING_DATASOURCE_PASSWORD";

    @AfterEach
    void cleanup() {
        System.clearProperty(URL_KEY);
        System.clearProperty(USER_KEY);
        System.clearProperty(PASS_KEY);
    }

    @Test
    void loadDbPropertiesFromSecretIfPresentSetsMissingValues() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_SECRET", "{\"username\":\"dbu\",\"password\":\"dbp\",\"host\":\"localhost\",\"dbname\":\"ibe\",\"port\":\"5432\"}");

        SecretsBootstrapConfig config = new SecretsBootstrapConfig(env);
        config.loadDbPropertiesFromSecretIfPresent();

        assertEquals("jdbc:postgresql://localhost:5432/ibe", System.getProperty(URL_KEY));
        assertEquals("dbu", System.getProperty(USER_KEY));
        assertEquals("dbp", System.getProperty(PASS_KEY));
    }

    @Test
    void loadDbPropertiesFromSecretIfPresentDoesNotOverrideExistingSystemProperty() {
        System.setProperty(USER_KEY, "already-set");
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_SECRET", "{\"username\":\"new-user\",\"password\":\"dbp\",\"host\":\"localhost\",\"dbname\":\"ibe\",\"port\":\"5432\"}");

        SecretsBootstrapConfig config = new SecretsBootstrapConfig(env);
        config.loadDbPropertiesFromSecretIfPresent();

        assertEquals("already-set", System.getProperty(USER_KEY));
    }

    @Test
    void loadDbPropertiesFromSecretIfPresentSkipsWhenSecretMissing() {
        MockEnvironment env = new MockEnvironment();
        SecretsBootstrapConfig config = new SecretsBootstrapConfig(env);

        config.loadDbPropertiesFromSecretIfPresent();

        assertNull(System.getProperty(URL_KEY));
        assertNull(System.getProperty(USER_KEY));
        assertNull(System.getProperty(PASS_KEY));
    }
}

