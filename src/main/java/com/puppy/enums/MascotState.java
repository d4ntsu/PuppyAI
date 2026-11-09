package com.puppy.enums;

/**
 * The six animations the mascot can display.
 *
 * <p>Each state maps to a folder of sprite frames under {@code /sprites/<state>/}.
 * A future milestone adds the priority rules that decide which state wins when
 * several parts of the application request one at the same time.</p>
 */
public enum MascotState {

    /** Default desktop state. */
    IDLE,

    /** Positive reactions. */
    HAPPY,

    /** Processing a request. */
    THINKING,

    /** Delivering a response. */
    TALKING,

    /** Study mode is active. */
    STUDYING,

    /** Idle for an extended period. */
    SLEEPING
}
