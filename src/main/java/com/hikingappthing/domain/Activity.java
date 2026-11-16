package com.hikingappthing.domain;

import java.time.Instant;
import java.util.List;

public record Activity(
        String id,
        String title,
        ActivityType type,
        Instant startTime,
        double distanceMeters,
        long durationSeconds,
        double elevationGainMeters,
        Double calories,
        List<RoutePoint> route,
        List<Peak> peaks
) {
}
