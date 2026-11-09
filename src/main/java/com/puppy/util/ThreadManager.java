package com.puppy.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns the background thread pool used by application services.
 *
 * <p>No backend work is scheduled in milestone 1; the pool exists so that later
 * milestones can submit AI, web and database work without touching the JavaFX
 * Application Thread, and so shutdown is handled in one place.</p>
 */
public final class ThreadManager implements AutoCloseable {

    private static final int POOL_SIZE = 4;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 3;

    private final ExecutorService executor;
    private volatile boolean shutdown;

    public ThreadManager(String appName) {
        ThreadFactory factory = new NamedDaemonThreadFactory(appName);
        this.executor = Executors.newFixedThreadPool(POOL_SIZE, factory);
    }

    /** The shared background executor. Never call this from the JavaFX Application Thread. */
    public ExecutorService executor() {
        return executor;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * Stops accepting work, waits briefly for running tasks and then forces the
     * remaining ones to stop. Safe to call more than once.
     */
    @Override
    public void close() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final class NamedDaemonThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger(1);
        private final String prefix;

        private NamedDaemonThreadFactory(String appName) {
            this.prefix = appName.replace(' ', '-').toLowerCase() + "-worker-";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, prefix + sequence.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
