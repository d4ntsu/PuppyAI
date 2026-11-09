package com.puppy.config;

import com.puppy.service.DefaultMascotStateService;
import com.puppy.service.MascotStateService;
import com.puppy.util.ThreadManager;

import java.util.Objects;

/**
 * Builds and owns the application's services.
 *
 * <p>JavaFX controllers receive services from here instead of creating databases,
 * repositories or AI providers themselves. Later milestones register persistence,
 * AI providers and research services in the same place, and {@link #close()}
 * shuts all of them down when the application exits.</p>
 */
public final class ServiceRegistry implements AutoCloseable {

    private final AppConfig config;
    private final ThreadManager threadManager;
    private final MascotStateService mascotStateService;

    private boolean started;
    private boolean closed;

    public ServiceRegistry(AppConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.threadManager = new ThreadManager(config.appName());
        this.mascotStateService = new DefaultMascotStateService();
    }

    /** Initializes the services that need explicit startup. Safe to call once. */
    public void start() {
        if (started || closed) {
            return;
        }
        started = true;
        // Later milestones: database connection, repositories, AI providers.
    }

    public boolean isStarted() {
        return started;
    }

    public AppConfig config() {
        return config;
    }

    public MascotStateService mascotStateService() {
        return mascotStateService;
    }

    public ThreadManager threadManager() {
        return threadManager;
    }

    /** Releases every service; called from {@code Application.stop()}. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        started = false;
        threadManager.close();
    }
}
