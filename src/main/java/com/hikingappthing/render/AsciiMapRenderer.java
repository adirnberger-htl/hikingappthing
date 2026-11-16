package com.hikingappthing.render;

import com.hikingappthing.config.RenderConfig;
import com.hikingappthing.domain.RoutePoint;

import java.util.List;

public class AsciiMapRenderer {
    private static final String RESET = "\u001B[0m";

    public String render(List<RoutePoint> route, RenderConfig config) {
        if (route == null || route.isEmpty()) {
            return "(No route geometry available for this activity)";
        }

        int width = config.getWidth();
        int height = config.getHeight();
        char[][] grid = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = ' ';
            }
        }

        double minLat = route.stream().mapToDouble(RoutePoint::lat).min().orElse(0.0);
        double maxLat = route.stream().mapToDouble(RoutePoint::lat).max().orElse(1.0);
        double minLon = route.stream().mapToDouble(RoutePoint::lon).min().orElse(0.0);
        double maxLon = route.stream().mapToDouble(RoutePoint::lon).max().orElse(1.0);

        int prevX = -1;
        int prevY = -1;
        for (RoutePoint point : route) {
            int x = normalize(point.lon(), minLon, maxLon, width - 1);
            int y = normalize(point.lat(), minLat, maxLat, height - 1);
            y = (height - 1) - y;
            plotLine(grid, prevX, prevY, x, y);
            prevX = x;
            prevY = y;
        }

        String color = "\u001B[" + config.getRouteColorAnsi() + "m";
        StringBuilder out = new StringBuilder();
        for (char[] row : grid) {
            for (char ch : row) {
                if (ch == '*') {
                    out.append(color).append('*').append(RESET);
                } else {
                    out.append(ch);
                }
            }
            out.append('\n');
        }
        return out.toString();
    }

    private int normalize(double value, double min, double max, int scale) {
        if (Math.abs(max - min) < 1e-9) {
            return 0;
        }
        double ratio = (value - min) / (max - min);
        int normalized = (int) Math.round(ratio * scale);
        if (normalized < 0) {
            return 0;
        }
        return Math.min(normalized, scale);
    }

    private void plotLine(char[][] grid, int x0, int y0, int x1, int y1) {
        if (x0 < 0 || y0 < 0) {
            put(grid, x1, y1);
            return;
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0;
        int y = y0;
        while (true) {
            put(grid, x, y);
            if (x == x1 && y == y1) {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private void put(char[][] grid, int x, int y) {
        if (y >= 0 && y < grid.length && x >= 0 && x < grid[0].length) {
            grid[y][x] = '*';
        }
    }
}
