package com.hikingappthing.data;

import com.hikingappthing.domain.Activity;

import java.io.IOException;
import java.util.List;

public interface ActivityProvider {
    List<Activity> fetchRecentActivities(int limit) throws IOException, InterruptedException;
}
