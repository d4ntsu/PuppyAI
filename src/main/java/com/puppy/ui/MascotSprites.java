package com.puppy.ui;

import com.puppy.enums.MascotState;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes how the mascot spritesheet is laid out and which animation each
 * {@link MascotState} uses.
 *
 * <p>The sheet is a regular grid: {@value #COLUMNS} frames per row and one row
 * per mascot state, in {@link MascotState} declaration order. The frame size is
 * derived from the sheet's pixel size, so replacing the artwork with a sheet of
 * the same grid shape needs no code changes.</p>
 *
 * <pre>
 * row 0  IDLE      row 1  HAPPY     row 2  THINKING
 * row 3  TALKING   row 4  STUDYING  row 5  SLEEPING
 * </pre>
 */
public final class MascotSprites {

    /** Classpath location of the mascot spritesheet. */
    public static final String SHEET_RESOURCE = "/sprites/new_spritesheet.png";

    /** Frames per animation (columns in the sheet). */
    public static final int COLUMNS = 6;

    /** Animations in the sheet (rows). */
    public static final int ROWS = 6;

    /** Classpath location of the application icon. */
    public static final String ICON_RESOURCE = "/icons/puppy.png";

    /**
     * Timing for one mascot animation.
     *
     * @param row                 spritesheet row that holds the frames
     * @param frameCount          number of frames taken from that row
     * @param frameDurationMillis how long one frame is shown at normal speed
     */
    public record Animation(int row, int frameCount, double frameDurationMillis) {

        public Animation {
            if (row < 0 || row >= ROWS) {
                throw new IllegalArgumentException("row must be between 0 and " + (ROWS - 1));
            }
            if (frameCount < 1 || frameCount > COLUMNS) {
                throw new IllegalArgumentException("frameCount must be between 1 and " + COLUMNS);
            }
            if (!Double.isFinite(frameDurationMillis) || frameDurationMillis <= 0) {
                throw new IllegalArgumentException("frameDurationMillis must be positive");
            }
        }
    }

    private static final Map<MascotState, Animation> ANIMATIONS = animations();

    private MascotSprites() {
    }

    private static Map<MascotState, Animation> animations() {
        Map<MascotState, Animation> map = new EnumMap<>(MascotState.class);
        map.put(MascotState.IDLE, new Animation(0, COLUMNS, 220));
        map.put(MascotState.HAPPY, new Animation(1, COLUMNS, 130));
        map.put(MascotState.THINKING, new Animation(2, COLUMNS, 260));
        map.put(MascotState.TALKING, new Animation(3, COLUMNS, 110));
        map.put(MascotState.STUDYING, new Animation(4, COLUMNS, 400));
        map.put(MascotState.SLEEPING, new Animation(5, COLUMNS, 600));
        return Map.copyOf(map);
    }

    public static Animation animationFor(MascotState state) {
        Objects.requireNonNull(state, "state");
        return ANIMATIONS.get(state);
    }
}
