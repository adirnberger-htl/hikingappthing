package com.hikingappthing.state;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppState {
    private boolean authPromptShown;
    private int nextActivityIndex;

    public boolean isAuthPromptShown() {
        return authPromptShown;
    }

    public void setAuthPromptShown(boolean authPromptShown) {
        this.authPromptShown = authPromptShown;
    }

    public int getNextActivityIndex() {
        return nextActivityIndex;
    }

    public void setNextActivityIndex(int nextActivityIndex) {
        this.nextActivityIndex = nextActivityIndex;
    }
}
