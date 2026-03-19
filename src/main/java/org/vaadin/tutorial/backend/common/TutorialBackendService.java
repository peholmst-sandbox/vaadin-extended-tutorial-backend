package org.vaadin.tutorial.backend.common;

import java.time.Duration;

public abstract class TutorialBackendService {

    private final Duration artificialDelay;

    public TutorialBackendService(Duration artificialDelay) {
        this.artificialDelay = artificialDelay;
    }

    protected final void simulateDelay() {
        if (!artificialDelay.isZero() && !artificialDelay.isNegative()) {
            try {
                Thread.sleep(artificialDelay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during simulated delay", e);
            }
        }
    }
}
