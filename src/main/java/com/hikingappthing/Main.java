package com.hikingappthing;

import com.hikingappthing.config.AppConfig;
import com.hikingappthing.config.ConfigLoader;
import com.hikingappthing.data.ActivityProvider;
import com.hikingappthing.data.KomootExportActivityProvider;
import com.hikingappthing.data.TerraActivityProvider;
import com.hikingappthing.domain.Activity;
import com.hikingappthing.domain.ActivityType;
import com.hikingappthing.domain.Peak;
import com.hikingappthing.render.AsciiMapRenderer;
import com.hikingappthing.state.AppState;
import com.hikingappthing.state.StateStore;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class Main {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException, InterruptedException {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            printHelp();
            return;
        }

        AppConfig config = ConfigLoader.load();
        StateStore stateStore = new StateStore(Path.of(config.getStateFile()));
        AppState state = stateStore.load();

        ActivityProvider provider = buildProvider(config);
        List<Activity> activities = provider.fetchRecentActivities(config.getFetchLimit()).stream()
                .sorted(Comparator.comparing(Activity::startTime).reversed())
                .toList();

        if (activities.isEmpty()) {
            throw new IllegalStateException("No activities returned by data source.");
        }

        if (args.length > 0 && "peaks".equalsIgnoreCase(args[0])) {
            printPeaks(activities);
            return;
        }

        int index = Math.floorMod(state.getNextActivityIndex(), activities.size());
        Activity chosen = activities.get(index);

        state.setNextActivityIndex((index + 1) % activities.size());
        stateStore.save(state);

        printActivity(chosen, config);
    }

    private static ActivityProvider buildProvider(AppConfig config) {
        if ("komoot-export".equalsIgnoreCase(config.getDataSource())) {
            return new KomootExportActivityProvider(config.getKomootExport());
        }
        if ("terra".equalsIgnoreCase(config.getDataSource())) {
            return new TerraActivityProvider(java.net.http.HttpClient.newHttpClient(), config.getTerra());
        }
        else {
            throw new IllegalArgumentException("Unsupported dataSource: " + config.getDataSource());
        }
    }

    private static void printActivity(Activity activity, AppConfig config) {
        System.out.println(activity.title());
        System.out.println(activity.type() + " | " + DATE_FORMATTER.format(activity.startTime()));
        System.out.println(renderStats(activity));
        System.out.println();
        System.out.println(new AsciiMapRenderer().render(activity.route(), config.getRender()));
    }

    private static String renderStats(Activity activity) {
        double distanceKm = activity.distanceMeters() / 1000.0;
        double durationHours = activity.durationSeconds() / 3600.0;
        double elevation = activity.elevationGainMeters();
        String base = String.format(Locale.ROOT,
                "Distance: %.2f km | Duration: %s | Elevation+: %.0f m",
                distanceKm, formatDuration(activity.durationSeconds()), elevation);

        String paceOrSpeed;
        if (activity.type() == ActivityType.RUN) {
            double minutesPerKm = distanceKm <= 0.0 ? 0.0 : (activity.durationSeconds() / 60.0) / distanceKm;
            paceOrSpeed = String.format(Locale.ROOT, " | Avg Pace: %.2f min/km", minutesPerKm);
        } else {
            double speed = durationHours <= 0.0 ? 0.0 : distanceKm / durationHours;
            paceOrSpeed = String.format(Locale.ROOT, " | Avg Speed: %.2f km/h", speed);
        }

        String calories = activity.calories() == null
                ? ""
                : String.format(Locale.ROOT, " | Calories: %.0f", activity.calories());
        return base + paceOrSpeed + calories;
    }

    private static void printPeaks(List<Activity> activities) {
        List<PeakRow> peaks = activities.stream()
                .filter(a -> a.type() == ActivityType.HIKE)
                .flatMap(a -> a.peaks().stream().map(p -> new PeakRow(p, a.startTime().toEpochMilli())))
                .sorted(Comparator.comparing(PeakRow::timestamp).reversed())
                .toList();

        if (peaks.isEmpty()) {
            System.out.println("No peaks available for recent hike activities.");
            return;
        }

        System.out.println("Recent bagged peaks:");
        LinkedHashMap<String, Peak> byName = new LinkedHashMap<>();
        for (PeakRow row : peaks) {
            Peak peak = row.peak();
            if (peak == null || peak.name() == null || peak.name().isBlank() || byName.containsKey(peak.name())) {
                continue;
            }
            byName.put(peak.name(), peak);
        }

        List<Peak> sortedPeaks = byName.values().stream()
                .sorted(Comparator
                        .comparing(Peak::elevationMeters, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Peak::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (Peak peak : sortedPeaks) {
            String elevation = peak.elevationMeters() == null ? "?" : String.format(Locale.ROOT, "%.0f m", peak.elevationMeters());
            System.out.printf("- %s (%s)%n", peak.name(), elevation);
        }
    }

    private static String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  java -jar target/hikingappthing-0.1.0-SNAPSHOT.jar [peaks]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  (no args)   Show next activity map and stats");
        System.out.println("  peaks       Show recent peaks from hike activities");
    }

    private record PeakRow(Peak peak, long timestamp) {
    }
}
