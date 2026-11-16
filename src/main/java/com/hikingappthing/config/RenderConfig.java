package com.hikingappthing.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RenderConfig {
    private int width = 72;
    private int height = 20;
    private String routeColorAnsi = "38;2;255;165;0";
    private String backgroundProfile = "dark-purple";

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getRouteColorAnsi() {
        return routeColorAnsi;
    }

    public void setRouteColorAnsi(String routeColorAnsi) {
        this.routeColorAnsi = routeColorAnsi;
    }

    public String getBackgroundProfile() {
        return backgroundProfile;
    }

    public void setBackgroundProfile(String backgroundProfile) {
        this.backgroundProfile = backgroundProfile;
    }
}
