package com.puppy.service;

import com.puppy.enums.MascotState;

import java.util.function.Consumer;

/**
 * Single source of truth for the mascot's current animation state.
 *
 * <p>Every part of the application that wants to change the mascot goes through
 * this service, and the UI follows it by registering a listener. The priority
 * rules that prevent competing states from fighting each other arrive in a later
 * milestone; for now the most recent request wins.</p>
 */
public interface MascotStateService {

    void setState(MascotState state);

    MascotState getState();

    void resetToIdle();

    /**
     * Registers a listener that is notified whenever the state actually changes.
     * Listeners may be called from any thread, so UI listeners must marshal to the
     * JavaFX Application Thread.
     */
    void addListener(Consumer<MascotState> listener);

    void removeListener(Consumer<MascotState> listener);
}
