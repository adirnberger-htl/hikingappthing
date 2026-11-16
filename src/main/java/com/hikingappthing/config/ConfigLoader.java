package com.hikingappthing.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String ENV_CONFIG = "HIKING_APP_CONFIG";
    private static final String DEFAULT_CONFIG = "hikingappthing.config.json";

    private ConfigLoader() {
    }

    public static AppConfig load() throws IOException {
        Path path = resolveConfigPath();
        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "Config file not found: " + path + ". Copy hikingappthing.config.example.json to " + DEFAULT_CONFIG + ".");
        }

        AppConfig config = MAPPER.readValue(path.toFile(), AppConfig.class);
        validate(config);
        return config;
    }

    private static Path resolveConfigPath() {
        String configured = System.getenv(ENV_CONFIG);
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        return Path.of(DEFAULT_CONFIG);
    }

    private static void validate(AppConfig config) {
        if (config.getFetchLimit() <= 0) {
            throw new IllegalStateException("fetchLimit must be > 0.");
        }
        if (config.getRender().getWidth() < 20 || config.getRender().getHeight() < 8) {
            throw new IllegalStateException("render.width/render.height are too small for readable output.");
        }
        if ("komoot-export".equalsIgnoreCase(config.getDataSource())) {
            if (config.getKomootExport() == null
                    || config.getKomootExport().getExportsDir() == null
                    || config.getKomootExport().getExportsDir().isBlank()) {
                throw new IllegalStateException("komootExport.exportsDir is required for dataSource=komoot-export.");
            }
        }
    }
}
