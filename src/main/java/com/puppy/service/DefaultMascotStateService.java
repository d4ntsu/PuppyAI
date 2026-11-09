package com.puppy.service;

import com.puppy.enums.MascotState;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Thread-safe {@link MascotStateService} that notifies listeners on every
 * effective change.
 */
public final class DefaultMascotStateService implements MascotStateService {

    private final AtomicReference<MascotState> state = new AtomicReference<>(MascotState.IDLE);
    private final List<Consumer<MascotState>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void setState(MascotState state) {
        Objects.requireNonNull(state, "state");
        MascotState previous = this.state.getAndSet(state);
        if (previous == state) {
            return;
        }
        for (Consumer<MascotState> listener : listeners) {
            listener.accept(state);
        }
    }

    @Override
    public MascotState getState() {
        return state.get();
    }

    @Override
    public void resetToIdle() {
        setState(MascotState.IDLE);
    }

    @Override
    public void addListener(Consumer<MascotState> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    @Override
    public void removeListener(Consumer<MascotState> listener) {
        listeners.remove(listener);
    }
}
