package com.puppy.config;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Application-level configuration.
 *
 * <p>Values can be overridden with a system property such as
 * {@code -Dpuppy.alwaysOnTop=false} or with the environment variable
 * {@code PUPPY_ALWAYS_ON_TOP}. Runtime data (database, logs) will live in
 * {@link #dataDirectory()} once persistence is added in a later milestone.</p>
 */
public record AppConfig(
        String appName,
        String appVersion,
        boolean alwaysOnTop,
        Path dataDirectory
) {

    public static final String APP_NAME = "Puppy AI";
    public static final String APP_VERSION = "1.0.0-SNAPSHOT";

    public AppConfig {
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("appName must not be blank");
        }
        if (appVersion == null || appVersion.isBlank()) {
            throw new IllegalArgumentException("appVersion must not be blank");
        }
        if (dataDirectory == null) {
            throw new IllegalArgumentException("dataDirectory must not be null");
        }
    }

    /** Loads the configuration from system properties, environment and defaults. */
    public static AppConfig load() {
        return new AppConfig(
                APP_NAME,
                APP_VERSION,
                readBoolean("alwaysOnTop", true),
                defaultDataDirectory());
    }

    private static Path defaultDataDirectory() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path base = (localAppData != null && !localAppData.isBlank())
                ? Path.of(localAppData)
                : Path.of(System.getProperty("user.home"), ".local", "share");
        return base.resolve("PuppyAI");
    }

    private static boolean readBoolean(String key, boolean defaultValue) {
        String value = read(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private static String read(String key) {
        String property = System.getProperty("puppy." + key);
        if (property != null && !property.isBlank()) {
            return property.trim();
        }
        String environmentKey = "PUPPY_" + key.replace('.', '_').toUpperCase(Locale.ROOT);
        String environment = System.getenv(environmentKey);
        return environment == null || environment.isBlank() ? null : environment.trim();
    }
}
