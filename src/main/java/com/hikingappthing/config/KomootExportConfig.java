package com.hikingappthing.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KomootExportConfig {
    private String exportsDir = "komoot-exports";
    private boolean recursive = false;

    public String getExportsDir() {
        return exportsDir;
    }

    public void setExportsDir(String exportsDir) {
        this.exportsDir = exportsDir;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }
}
