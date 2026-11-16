package com.hikingappthing.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppConfig {
    private String dataSource = "komoot-export";
    private int fetchLimit = 30;
    private String stateFile = ".hikingappthing-state.json";
    private RenderConfig render = new RenderConfig();
    private KomootExportConfig komootExport = new KomootExportConfig();
    private TerraConfig terra = new TerraConfig();

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public int getFetchLimit() {
        return fetchLimit;
    }

    public void setFetchLimit(int fetchLimit) {
        this.fetchLimit = fetchLimit;
    }

    public String getStateFile() {
        return stateFile;
    }

    public void setStateFile(String stateFile) {
        this.stateFile = stateFile;
    }

    public RenderConfig getRender() {
        return render;
    }

    public void setRender(RenderConfig render) {
        this.render = render;
    }

    public KomootExportConfig getKomootExport() {
        return komootExport;
    }

    public void setKomootExport(KomootExportConfig komootExport) {
        this.komootExport = komootExport;
    }

    public TerraConfig getTerra() {
        return terra;
    }

    public void setTerra(TerraConfig terra) {
        this.terra = terra;
    }
}
