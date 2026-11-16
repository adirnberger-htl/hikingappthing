package com.hikingappthing.render;

import com.hikingappthing.config.RenderConfig;
import com.hikingappthing.domain.RoutePoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsciiMapRendererTest {
    @Test
    void rendersColoredRoute() {
        RenderConfig config = new RenderConfig();
        config.setWidth(20);
        config.setHeight(8);

        List<RoutePoint> route = List.of(
                new RoutePoint(47.0, 11.0),
                new RoutePoint(47.1, 11.1),
                new RoutePoint(47.2, 11.2)
        );

        String output = new AsciiMapRenderer().render(route, config);
        assertTrue(output.contains("\u001B[38;2;255;165;0m*\u001B[0m"));
    }

    @Test
    void rendersNoGeometryMessage() {
        String output = new AsciiMapRenderer().render(List.of(), new RenderConfig());
        assertEquals("(No route geometry available for this activity)", output);
    }
}
