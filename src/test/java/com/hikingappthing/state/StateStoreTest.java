package com.hikingappthing.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateStoreTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndLoadsState() throws Exception {
        Path path = tempDir.resolve("state.json");
        StateStore store = new StateStore(path);

        AppState state = new AppState();
        state.setAuthPromptShown(true);
        state.setNextActivityIndex(7);
        store.save(state);

        AppState loaded = store.load();
        assertTrue(loaded.isAuthPromptShown());
        assertEquals(7, loaded.getNextActivityIndex());
    }
}
