package com.puppy.service;

import com.puppy.enums.MascotState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMascotStateServiceTest {

    private final DefaultMascotStateService service = new DefaultMascotStateService();

    @Test
    void startsIdle() {
        assertEquals(MascotState.IDLE, service.getState());
    }

    @Test
    void notifiesListenersOnChange() {
        List<MascotState> received = new ArrayList<>();
        service.addListener(received::add);

        service.setState(MascotState.THINKING);
        service.setState(MascotState.TALKING);

        assertEquals(List.of(MascotState.THINKING, MascotState.TALKING), received);
        assertEquals(MascotState.TALKING, service.getState());
    }

    @Test
    void doesNotNotifyWhenStateIsUnchanged() {
        List<MascotState> received = new ArrayList<>();
        service.addListener(received::add);

        service.setState(MascotState.HAPPY);
        service.setState(MascotState.HAPPY);

        assertEquals(List.of(MascotState.HAPPY), received);
    }

    @Test
    void resetToIdleReturnsToDefaultState() {
        service.setState(MascotState.STUDYING);

        service.resetToIdle();

        assertEquals(MascotState.IDLE, service.getState());
    }

    @Test
    void removedListenerStopsReceivingUpdates() {
        List<MascotState> received = new ArrayList<>();
        Consumer<MascotState> listener = received::add;
        service.addListener(listener);

        service.setState(MascotState.SLEEPING);
        service.removeListener(listener);
        service.setState(MascotState.HAPPY);

        assertEquals(List.of(MascotState.SLEEPING), received);
    }

    @Test
    void listenerCanBeNotifiedFromAnotherThread() throws InterruptedException {
        List<MascotState> received = new ArrayList<>();
        service.addListener(received::add);

        Thread worker = new Thread(() -> service.setState(MascotState.THINKING));
        worker.start();
        worker.join();

        assertTrue(received.contains(MascotState.THINKING));
    }
}
