package com.puppy.service;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Countdown logic for a Pomodoro focus/break session.
 *
 * <p>Remaining time is derived from a target timestamp instead of counting timer
 * ticks, so a slow or paused UI cannot make the timer drift. The nano clock is
 * injectable so the behaviour can be tested without waiting in real time.</p>
 *
 * <p>Pure Java: no JavaFX and no threads. The UI drives it from a timeline and
 * calls {@link #start()}, {@link #pause()}, {@link #reset()} and
 * {@link #completePhase()} in response to user actions and completion.</p>
 */
public final class StudyTimer {

    public static final int DEFAULT_FOCUS_SECONDS = 25 * 60;
    public static final int DEFAULT_BREAK_SECONDS = 5 * 60;
    public static final int TEST_MODE_SECONDS = 60;

    /** Which half of the cycle the session is in. */
    public enum Phase {
        FOCUS,
        BREAK
    }

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final int focusSeconds;
    private final int breakSeconds;
    private final LongSupplier nanoClock;

    private Phase phase = Phase.FOCUS;
    private boolean running;
    private boolean started;
    private long remainingNanos;
    private long targetNanos;

    /** A 25-minute focus session followed by a 5-minute break. */
    public StudyTimer() {
        this(DEFAULT_FOCUS_SECONDS, DEFAULT_BREAK_SECONDS, System::nanoTime);
    }

    /** A timer with custom durations, e.g. {@code TEST_MODE_SECONDS} for testing. */
    public StudyTimer(int focusSeconds, int breakSeconds) {
        this(focusSeconds, breakSeconds, System::nanoTime);
    }

    StudyTimer(int focusSeconds, int breakSeconds, LongSupplier nanoClock) {
        if (focusSeconds <= 0 || breakSeconds <= 0) {
            throw new IllegalArgumentException("Durations must be positive");
        }
        this.focusSeconds = focusSeconds;
        this.breakSeconds = breakSeconds;
        this.nanoClock = Objects.requireNonNull(nanoClock, "nanoClock");
        this.remainingNanos = focusSeconds * NANOS_PER_SECOND;
    }

    public Phase getPhase() {
        return phase;
    }

    public boolean isRunning() {
        return running;
    }

    /** False until the current phase has been started since the last reset. */
    public boolean isStarted() {
        return started;
    }

    /** Whole seconds left, rounded up so a fresh 25:00 session reads 25:00. */
    public int getRemainingSeconds() {
        long remaining = remainingNanos();
        return (int) ((remaining + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND);
    }

    public boolean isFinished() {
        return remainingNanos() <= 0;
    }

    /** Starts or resumes the current phase. Does nothing once that phase has ended. */
    public void start() {
        if (running || isFinished()) {
            return;
        }
        running = true;
        started = true;
        targetNanos = nanoClock.getAsLong() + remainingNanos;
    }

    /** Freezes the countdown; remaining time is preserved. */
    public void pause() {
        if (!running) {
            return;
        }
        remainingNanos = remainingNanos();
        running = false;
    }

    /** Returns to a fresh focus session and clears the started flag. */
    public void reset() {
        phase = Phase.FOCUS;
        running = false;
        started = false;
        remainingNanos = focusSeconds * NANOS_PER_SECOND;
        targetNanos = 0;
    }

    /**
     * Ends the current phase, advances to the other one and leaves it ready to
     * start. Called by the UI when {@link #isFinished()} becomes true.
     */
    public void completePhase() {
        running = false;
        started = false;
        if (phase == Phase.FOCUS) {
            phase = Phase.BREAK;
            remainingNanos = breakSeconds * NANOS_PER_SECOND;
        } else {
            phase = Phase.FOCUS;
            remainingNanos = focusSeconds * NANOS_PER_SECOND;
        }
        targetNanos = 0;
    }

    private long remainingNanos() {
        if (!running) {
            return remainingNanos;
        }
        return Math.max(0, targetNanos - nanoClock.getAsLong());
    }
}
