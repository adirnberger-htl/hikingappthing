package com.hikingappthing.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hikingappthing.config.TerraConfig;
import com.hikingappthing.domain.Activity;
import com.hikingappthing.domain.ActivityType;
import com.hikingappthing.domain.Peak;
import com.hikingappthing.domain.RoutePoint;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TerraActivityProvider implements ActivityProvider {
    private final HttpClient httpClient;
    private final TerraConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    public TerraActivityProvider(HttpClient httpClient, TerraConfig config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public List<Activity> fetchRecentActivities(int limit) throws IOException, InterruptedException {
        String separator = config.getActivitiesUrl().contains("?") ? "&" : "?";
        String url = config.getActivitiesUrl()
                + separator + "user_id=" + URLEncoder.encode(config.getUserId(), StandardCharsets.UTF_8)
                + "&limit=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("dev-id", config.getDevId())
                .header("x-api-key", config.getApiKey())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new IllegalStateException("Terra API returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode activitiesNode = root.isArray() ? root : root.path("data");
        if (!activitiesNode.isArray()) {
            throw new IllegalStateException("Unsupported Terra response shape: expected array or {data:[...]}");
        }

        List<Activity> activities = new ArrayList<>();
        for (JsonNode node : activitiesNode) {
            activities.add(mapActivity(node));
        }
        return activities;
    }

    private Activity mapActivity(JsonNode node) {
        String id = text(node, "id", "activity_id", "uuid");
        String title = text(node, "name", "title");
        ActivityType type = mapType(text(node, "activity_type", "type", "sport"));
        Instant start = parseInstant(text(node, "start_time", "start_date", "created_at"));
        double distance = number(node, "distance_meters", "distance", "distance_metres");
        long duration = (long) number(node, "duration_seconds", "moving_time", "duration");
        double elevation = number(node, "elevation_gain_meters", "total_elevation_gain", "elevation_gain");
        Double calories = nullableNumber(node, "calories", "calories_burned");

        List<RoutePoint> route = parseRoute(node);
        List<Peak> peaks = parsePeaks(node);

        String resolvedTitle = (title == null || title.isBlank()) ? type.name() + " activity" : title;
        return new Activity(
                id == null ? "unknown-" + start.toEpochMilli() : id,
                resolvedTitle,
                type,
                start,
                distance,
                duration,
                elevation,
                calories,
                route,
                peaks
        );
    }

    private List<RoutePoint> parseRoute(JsonNode node) {
        JsonNode points = node.path("route").path("points");
        if (!points.isArray()) {
            points = node.path("points");
        }

        List<RoutePoint> route = new ArrayList<>();
        if (points.isArray()) {
            for (JsonNode point : points) {
                double lat = number(point, "lat", "latitude");
                double lon = number(point, "lon", "lng", "longitude");
                if (lat != 0.0 || lon != 0.0) {
                    route.add(new RoutePoint(lat, lon));
                }
            }
            return route;
        }

        JsonNode coordinates = node.path("geometry").path("coordinates");
        if (coordinates.isArray()) {
            for (JsonNode coordinate : coordinates) {
                if (coordinate.isArray() && coordinate.size() >= 2) {
                    route.add(new RoutePoint(coordinate.get(1).asDouble(), coordinate.get(0).asDouble()));
                }
            }
        }
        return route;
    }

    private List<Peak> parsePeaks(JsonNode node) {
        JsonNode peaks = node.path("peaks");
        if (!peaks.isArray()) {
            return Collections.emptyList();
        }

        List<Peak> result = new ArrayList<>();
        for (JsonNode peak : peaks) {
            String name = text(peak, "name");
            if (name == null || name.isBlank()) {
                continue;
            }
            result.add(new Peak(
                    name,
                    nullableNumber(peak, "elevation_meters", "elevation"),
                    nullableNumber(peak, "lat", "latitude"),
                    nullableNumber(peak, "lon", "lng", "longitude")
            ));
        }
        return result;
    }

    private ActivityType mapType(String rawType) {
        if (rawType == null) {
            return ActivityType.OTHER;
        }
        String normalized = rawType.toLowerCase(Locale.ROOT);
        if (normalized.contains("hike") || normalized.contains("walk")) {
            return ActivityType.HIKE;
        }
        if (normalized.contains("run")) {
            return ActivityType.RUN;
        }
        if (normalized.contains("bike") || normalized.contains("ride") || normalized.contains("cycle")) {
            return ActivityType.BIKE;
        }
        return ActivityType.OTHER;
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return Instant.EPOCH;
        }
    }

    private String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.path(key);
            if (child.isTextual() && !child.asText().isBlank()) {
                return child.asText();
            }
        }
        return null;
    }

    private double number(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.path(key);
            if (child.isNumber()) {
                return child.asDouble();
            }
        }
        return 0.0;
    }

    private Double nullableNumber(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.path(key);
            if (child.isNumber()) {
                return child.asDouble();
            }
        }
        return null;
    }
}
