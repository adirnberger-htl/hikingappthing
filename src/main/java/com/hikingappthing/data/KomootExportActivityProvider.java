package com.hikingappthing.data;

import com.hikingappthing.config.KomootExportConfig;
import com.hikingappthing.domain.Activity;
import com.hikingappthing.domain.ActivityType;
import com.hikingappthing.domain.Peak;
import com.hikingappthing.domain.RoutePoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class KomootExportActivityProvider implements ActivityProvider {
    private final KomootExportConfig config;

    public KomootExportActivityProvider(KomootExportConfig config) {
        this.config = config;
    }

    @Override
    public List<Activity> fetchRecentActivities(int limit) throws IOException {
        Path exportDir = Path.of(config.getExportsDir());
        if (!Files.exists(exportDir) || !Files.isDirectory(exportDir)) {
            throw new IllegalStateException("Komoot export directory not found: " + exportDir.toAbsolutePath());
        }

        int maxDepth = config.isRecursive() ? Integer.MAX_VALUE : 1;
        List<Activity> activities = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(exportDir, maxDepth)) {
            List<Path> gpxFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isGpxFile)
                    .toList();
            for (Path gpxFile : gpxFiles) {
                activities.add(parseGpx(gpxFile));
            }
        }

        return activities.stream()
                .sorted(Comparator.comparing(Activity::startTime).reversed())
                .limit(limit)
                .toList();
    }

    private boolean isGpxFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gpx");
    }

    private Activity parseGpx(Path path) throws IOException {
        Document document = readXml(path);
        Element root = document.getDocumentElement();

        String title = firstText(root, "metadata", "name");
        if (title == null || title.isBlank()) {
            title = firstText(root, "trk", "name");
        }
        String fileName = path.getFileName().toString();
        if (title == null || title.isBlank()) {
            title = fileName.replaceFirst("\\.gpx$", "");
        }

        ActivityType type = inferType(title + " " + fileName);
        List<GpxPoint> points = parseTrackPoints(root);
        List<RoutePoint> route = points.stream().map(p -> new RoutePoint(p.lat(), p.lon())).toList();
        List<Peak> peaks = parseWaypoints(root);
        if (peaks.isEmpty() && type == ActivityType.HIKE) {
            Peak inferredPeak = inferRouteHighPointPeak(title, points);
            if (inferredPeak != null) {
                peaks.add(inferredPeak);
            }
        }

        Instant startTime = points.stream()
                .map(GpxPoint::time)
                .filter(t -> t != null)
                .findFirst()
                .orElse(fileTimestamp(path));

        Instant endTime = points.stream()
                .map(GpxPoint::time)
                .filter(t -> t != null)
                .reduce((first, second) -> second)
                .orElse(startTime);

        long durationSeconds = Math.max(0, endTime.getEpochSecond() - startTime.getEpochSecond());
        double distanceMeters = computeDistanceMeters(points);
        double elevationGainMeters = computeElevationGain(points);

        return new Activity(
                path.toAbsolutePath().toString(),
                title,
                type,
                startTime,
                distanceMeters,
                durationSeconds,
                elevationGainMeters,
                null,
                route,
                peaks
        );
    }

    private Document readXml(Path path) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newDocumentBuilder().parse(path.toFile());
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse GPX file: " + path, e);
        }
    }

    private List<GpxPoint> parseTrackPoints(Element root) {
        NodeList trkpts = root.getElementsByTagName("trkpt");
        if (trkpts.getLength() == 0) {
            trkpts = root.getElementsByTagName("rtept");
        }

        List<GpxPoint> points = new ArrayList<>();
        for (int i = 0; i < trkpts.getLength(); i++) {
            Element point = (Element) trkpts.item(i);
            double lat = parseDouble(point.getAttribute("lat"));
            double lon = parseDouble(point.getAttribute("lon"));
            Double ele = parseNullableDouble(textContent(point, "ele"));
            Instant time = parseNullableInstant(textContent(point, "time"));
            points.add(new GpxPoint(lat, lon, ele, time));
        }
        return points;
    }

    private List<Peak> parseWaypoints(Element root) {
        NodeList wpts = root.getElementsByTagName("wpt");
        List<Peak> peaks = new ArrayList<>();
        for (int i = 0; i < wpts.getLength(); i++) {
            Element waypoint = (Element) wpts.item(i);
            String name = textContent(waypoint, "name");
            if (name == null || name.isBlank()) {
                continue;
            }
            peaks.add(new Peak(
                    name,
                    parseNullableDouble(textContent(waypoint, "ele")),
                    parseNullableDouble(waypoint.getAttribute("lat")),
                    parseNullableDouble(waypoint.getAttribute("lon"))
            ));
        }
        return peaks;
    }

    private Peak inferRouteHighPointPeak(String title, List<GpxPoint> points) {
        GpxPoint highestPoint = null;
        for (GpxPoint point : points) {
            if (point.elevation() == null) {
                continue;
            }
            if (highestPoint == null || point.elevation() > highestPoint.elevation()) {
                highestPoint = point;
            }
        }
        if (highestPoint == null) {
            return null;
        }
        String peakName = (title == null || title.isBlank()) ? "Route high point" : title;
        return new Peak(peakName, highestPoint.elevation(), highestPoint.lat(), highestPoint.lon());
    }

    private double computeDistanceMeters(List<GpxPoint> points) {
        if (points.size() < 2) {
            return 0.0;
        }
        double meters = 0.0;
        for (int i = 1; i < points.size(); i++) {
            meters += haversine(points.get(i - 1).lat(), points.get(i - 1).lon(), points.get(i).lat(), points.get(i).lon());
        }
        return meters;
    }

    private double computeElevationGain(List<GpxPoint> points) {
        double gain = 0.0;
        for (int i = 1; i < points.size(); i++) {
            Double prev = points.get(i - 1).elevation();
            Double cur = points.get(i).elevation();
            if (prev == null || cur == null) {
                continue;
            }
            double diff = cur - prev;
            if (diff > 0) {
                gain += diff;
            }
        }
        return gain;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6_371_000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private ActivityType inferType(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        if (normalized.contains("hike") || normalized.contains("walk") || normalized.contains("wander")) {
            return ActivityType.HIKE;
        }
        if (normalized.contains("run") || normalized.contains("jog")) {
            return ActivityType.RUN;
        }
        if (normalized.contains("bike") || normalized.contains("cycle") || normalized.contains("ride")) {
            return ActivityType.BIKE;
        }
        return ActivityType.OTHER;
    }

    private String firstText(Element root, String parentTag, String childTag) {
        NodeList parents = root.getElementsByTagName(parentTag);
        if (parents.getLength() == 0) {
            return null;
        }
        Element parent = (Element) parents.item(0);
        return textContent(parent, childTag);
    }

    private String textContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = nodes.item(0).getTextContent();
        return value == null ? null : value.trim();
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }

    private Double parseNullableDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Instant parseNullableInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Instant fileTimestamp(Path path) throws IOException {
        FileTime modified = Files.getLastModifiedTime(path);
        return modified.toInstant();
    }

    private record GpxPoint(double lat, double lon, Double elevation, Instant time) {
    }
}
