package com.hikingappthing.state;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StateStore {
    private final Path path;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public StateStore(Path path) {
        this.path = path;
    }

    public AppState load() throws IOException {
        if (!Files.exists(path)) {
            return new AppState();
        }
        return mapper.readValue(path.toFile(), AppState.class);
    }

    public void save(AppState state) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), state);
    }
}
