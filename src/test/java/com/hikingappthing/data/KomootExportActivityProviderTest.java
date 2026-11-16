package com.hikingappthing.data;

import com.hikingappthing.config.KomootExportConfig;
import com.hikingappthing.domain.Activity;
import com.hikingappthing.domain.ActivityType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KomootExportActivityProviderTest {
    @TempDir
    Path tempDir;

    @Test
    void parsesGpxExportIntoActivity() throws Exception {
        Path gpx = tempDir.resolve("my-hike.gpx");
        Files.writeString(gpx, """
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.1" creator="komoot">
                  <metadata><name>Sunday Hike</name></metadata>
                  <wpt lat="47.0000" lon="11.0000"><name>Peak Alpha</name><ele>2100</ele></wpt>
                  <trk>
                    <name>Sunday Hike</name>
                    <trkseg>
                      <trkpt lat="47.0000" lon="11.0000"><ele>1800</ele><time>2026-05-18T08:00:00Z</time></trkpt>
                      <trkpt lat="47.0100" lon="11.0100"><ele>1900</ele><time>2026-05-18T08:30:00Z</time></trkpt>
                    </trkseg>
                  </trk>
                </gpx>
                """);

        KomootExportConfig config = new KomootExportConfig();
        config.setExportsDir(tempDir.toString());
        KomootExportActivityProvider provider = new KomootExportActivityProvider(config);

        List<Activity> activities = provider.fetchRecentActivities(10);
        assertEquals(1, activities.size());

        Activity activity = activities.get(0);
        assertEquals(ActivityType.HIKE, activity.type());
        assertEquals("Sunday Hike", activity.title());
        assertEquals(1800, activity.durationSeconds());
        assertFalse(activity.route().isEmpty());
        assertEquals(1, activity.peaks().size());
        assertEquals("Peak Alpha", activity.peaks().get(0).name());
        assertTrue(activity.distanceMeters() > 0.0);
        assertTrue(activity.elevationGainMeters() > 0.0);
    }

    @Test
    void infersPeakFromTrackHighPointForHikeWithoutWaypoints() throws Exception {
        Path gpx = tempDir.resolve("kasbergalm-hike.gpx");
        Files.writeString(gpx, """
                <?xml version="1.0" encoding="UTF-8"?>
                <gpx version="1.1" creator="komoot">
                  <metadata><name>Kasbergalm hike</name></metadata>
                  <trk>
                    <name>Kasbergalm hike</name>
                    <trkseg>
                      <trkpt lat="47.0000" lon="11.0000"><ele>1180</ele><time>2026-05-18T08:00:00Z</time></trkpt>
                      <trkpt lat="47.0100" lon="11.0100"><ele>1275</ele><time>2026-05-18T08:30:00Z</time></trkpt>
                    </trkseg>
                  </trk>
                </gpx>
                """);

        KomootExportConfig config = new KomootExportConfig();
        config.setExportsDir(tempDir.toString());
        KomootExportActivityProvider provider = new KomootExportActivityProvider(config);

        List<Activity> activities = provider.fetchRecentActivities(10);
        assertEquals(1, activities.size());

        Activity activity = activities.get(0);
        assertEquals(ActivityType.HIKE, activity.type());
        assertEquals(1, activity.peaks().size());
        assertEquals("Kasbergalm hike", activity.peaks().get(0).name());
        assertEquals(1275.0, activity.peaks().get(0).elevationMeters());
    }
}
