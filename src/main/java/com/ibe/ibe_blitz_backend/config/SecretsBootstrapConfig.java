package com.ibe.ibe_blitz_backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecretsBootstrapConfig {
    private final Environment env;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecretsBootstrapConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void loadDbPropertiesFromSecretIfPresent() {
        String dbSecret = env.getProperty("DB_SECRET");
        if (dbSecret == null || dbSecret.isBlank()) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(dbSecret);
            String username = text(root, "username");
            String password = text(root, "password");
            String host = text(root, "host");
            String dbName = text(root, "dbname");
            String port = text(root, "port");

            if (port == null || port.isBlank()) {
                port = "5432";
            }

            if (host != null && dbName != null) {
                setIfMissing("SPRING_DATASOURCE_URL", "jdbc:postgresql://" + host + ":" + port + "/" + dbName);
            }
            setIfMissing("SPRING_DATASOURCE_USERNAME", username);
            setIfMissing("SPRING_DATASOURCE_PASSWORD", password);
        } catch (Exception ignored) {
            // Fall back to existing datasource values if secret parsing fails.
        }
    }

    private static String text(JsonNode root, String key) {
        JsonNode node = root.get(key);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static void setIfMissing(String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String existing = System.getenv(key);
        String existingProperty = System.getProperty(key);
        if ((existing == null || existing.isBlank()) && (existingProperty == null || existingProperty.isBlank())) {
            System.setProperty(key, value);
        }
    }
}

