package com.project.notificationservice.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private TimeUtil() {
    }

    public static String now() {
        return FORMAT.format(Instant.now());
    }

    public static String format(Instant instant) {
        return FORMAT.format(instant);
    }
}
