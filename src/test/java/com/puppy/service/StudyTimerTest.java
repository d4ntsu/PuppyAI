package com.puppy.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives {@link StudyTimer} with a fake nano clock, so countdown behaviour is
 * tested in microseconds instead of real minutes.
 */
class StudyTimerTest {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private final AtomicLong clockNanos = new AtomicLong();
    private final StudyTimer timer = new StudyTimer(1500, 300, clockNanos::get);

    @Test
    void freshSessionIsReadyOnFocus() {
        assertEquals(StudyTimer.Phase.FOCUS, timer.getPhase());
        assertEquals(1500, timer.getRemainingSeconds());
        assertFalse(timer.isRunning());
        assertFalse(timer.isStarted());
        assertFalse(timer.isFinished());
    }

    @Test
    void startCountsDownFromTheTargetTimestamp() {
        timer.start();

        advanceSeconds(10);

        assertTrue(timer.isRunning());
        assertTrue(timer.isStarted());
        assertEquals(1490, timer.getRemainingSeconds());
    }

    @Test
    void pauseFreezesRemainingTime() {
        timer.start();
        advanceSeconds(10);

        timer.pause();
        advanceSeconds(100);

        assertFalse(timer.isRunning());
        assertEquals(1490, timer.getRemainingSeconds());
    }

    @Test
    void resumeContinuesWherePaused() {
        timer.start();
        advanceSeconds(10);
        timer.pause();
        advanceSeconds(100);

        timer.start();
        advanceSeconds(5);

        assertEquals(1485, timer.getRemainingSeconds());
    }

    @Test
    void resetReturnsToAFreshFocusSession() {
        timer.start();
        advanceSeconds(100);

        timer.reset();

        assertEquals(StudyTimer.Phase.FOCUS, timer.getPhase());
        assertEquals(1500, timer.getRemainingSeconds());
        assertFalse(timer.isRunning());
        assertFalse(timer.isStarted());
    }

    @Test
    void timerFinishesAtZero() {
        timer.start();

        advanceSeconds(1500);

        assertTrue(timer.isFinished());
        assertEquals(0, timer.getRemainingSeconds());
    }

    @Test
    void finishedPhaseCannotBeRestarted() {
        timer.start();
        advanceSeconds(1500);
        timer.pause(); // freeze the countdown at zero before trying to restart

        timer.start();

        assertTrue(timer.isFinished());
        assertFalse(timer.isRunning());
    }

    @Test
    void completingFocusPreparesTheBreak() {
        timer.start();
        advanceSeconds(1500);

        timer.completePhase();

        assertEquals(StudyTimer.Phase.BREAK, timer.getPhase());
        assertEquals(300, timer.getRemainingSeconds());
        assertFalse(timer.isRunning());
        assertFalse(timer.isStarted());

        timer.start();
        advanceSeconds(299);
        assertEquals(1, timer.getRemainingSeconds());
    }

    @Test
    void completingBreakReturnsToFocus() {
        timer.completePhase(); // focus -> break
        timer.start();
        advanceSeconds(300);

        timer.completePhase();

        assertEquals(StudyTimer.Phase.FOCUS, timer.getPhase());
        assertEquals(1500, timer.getRemainingSeconds());
        assertFalse(timer.isRunning());
    }

    @Test
    void supportsOneMinuteTestDurations() {
        StudyTimer testTimer = new StudyTimer(
                StudyTimer.TEST_MODE_SECONDS, StudyTimer.TEST_MODE_SECONDS, clockNanos::get);

        assertEquals(60, testTimer.getRemainingSeconds());
        testTimer.start();
        advanceSeconds(59);
        assertEquals(1, testTimer.getRemainingSeconds());
    }

    @Test
    void rejectsNonPositiveDurations() {
        assertThrows(IllegalArgumentException.class, () -> new StudyTimer(0, 300));
        assertThrows(IllegalArgumentException.class, () -> new StudyTimer(1500, 0));
    }

    private void advanceSeconds(int seconds) {
        clockNanos.addAndGet(seconds * NANOS_PER_SECOND);
    }
}
