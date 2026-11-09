package com.puppy.ui;

import com.puppy.enums.MascotState;
import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Plays mascot sprite animations on an {@link ImageView}.
 *
 * <p>The spritesheet is loaded once and each animation is played by moving the
 * image view's viewport to the active frame cell, so switching states never
 * reloads or copies pixels. Frame timing comes from {@link MascotSprites}.</p>
 *
 * <p>Must be used on the JavaFX Application Thread.</p>
 */
public final class MascotAnimator {

    private final ImageView target;
    private final Image sheet;
    private final int frameWidth;
    private final int frameHeight;
    private final AnimationTimer timer;

    private MascotState currentState;
    private int currentRow;
    private int frameIndex;
    private long currentFrameDurationNanos;
    private long lastFrameNanos;
    private double speed = 1.0;
    private boolean paused;
    private boolean started;

    public MascotAnimator(ImageView target) {
        this.target = Objects.requireNonNull(target, "target");
        this.sheet = loadSheet();
        this.frameWidth = frameSize(sheet.getWidth(), MascotSprites.COLUMNS);
        this.frameHeight = frameSize(sheet.getHeight(), MascotSprites.ROWS);

        target.setImage(sheet);
        applyFrame();

        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                advance(now);
            }
        };
    }

    /**
     * Starts (or restarts) the animation for the given state. Playing the state that
     * is already running only resumes playback, so callers can safely repeat events.
     */
    public void play(MascotState state) {
        Objects.requireNonNull(state, "state");
        MascotSprites.Animation animation = MascotSprites.animationFor(state);
        boolean stateChanged = state != currentState;

        currentState = state;
        currentRow = animation.row();
        currentFrameDurationNanos = Math.round(animation.frameDurationMillis() * 1_000_000);

        if (stateChanged) {
            frameIndex = 0;
            applyFrame();
        }
        paused = false;
        lastFrameNanos = 0;
        timer.start();
        started = true;
    }

    /** Freezes the current frame without forgetting the animation. */
    public void pause() {
        paused = true;
    }

    /** Continues a paused animation. */
    public void resume() {
        if (currentState == null) {
            return;
        }
        paused = false;
        lastFrameNanos = 0;
    }

    /** Stops playback and clears the image view. */
    public void stop() {
        timer.stop();
        started = false;
        currentState = null;
        frameIndex = 0;
        target.setImage(null);
    }

    /** Multiplier for animation speed: 2.0 plays twice as fast, 0.5 half as fast. */
    public void setSpeed(double speed) {
        if (!Double.isFinite(speed) || speed <= 0) {
            throw new IllegalArgumentException("speed must be a positive finite number");
        }
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public MascotState getCurrentState() {
        return currentState;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public boolean isRunning() {
        return started && !paused;
    }

    private void advance(long now) {
        if (paused || currentState == null) {
            return;
        }
        if (lastFrameNanos == 0) {
            lastFrameNanos = now;
            return;
        }

        int frameCount = MascotSprites.animationFor(currentState).frameCount();
        long frameDuration = Math.max(1, Math.round(currentFrameDurationNanos / speed));
        long elapsed = now - lastFrameNanos;
        if (elapsed < frameDuration) {
            return;
        }

        long framesToAdvance = elapsed / frameDuration;
        frameIndex = (int) ((frameIndex + framesToAdvance) % frameCount);
        lastFrameNanos += framesToAdvance * frameDuration;
        applyFrame();
    }

    private void applyFrame() {
        target.setViewport(new Rectangle2D(
                frameIndex * frameWidth,
                currentRow * frameHeight,
                frameWidth,
                frameHeight));
    }

    private static int frameSize(double sheetSize, int cells) {
        int size = (int) Math.round(sheetSize);
        if (size <= 0) {
            throw new IllegalStateException("Mascot spritesheet is empty");
        }
        if (size % cells != 0) {
            throw new IllegalStateException(
                    "Mascot spritesheet size " + size + "px does not divide into " + cells + " cells");
        }
        return size / cells;
    }

    private static Image loadSheet() {
        try (InputStream stream = MascotAnimator.class.getResourceAsStream(MascotSprites.SHEET_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Missing mascot spritesheet on classpath: " + MascotSprites.SHEET_RESOURCE);
            }
            return new Image(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read mascot spritesheet", e);
        }
    }
}
