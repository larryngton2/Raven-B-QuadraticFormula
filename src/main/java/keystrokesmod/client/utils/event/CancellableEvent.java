package keystrokesmod.client.utils.event;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * An abstract class for cancellable events.
 * This class implements the {@link Event} and {@link Cancellable} interfaces.
 */
public abstract class CancellableEvent extends Event implements Cancellable {
    /**
     * A flag indicating whether the event has been cancelled.
     */
    private boolean cancelled;

    /**
     * Sets the cancellation status of the event.
     *
     * @param cancelled {@code true} to cancel the event, {@code false} to allow it.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Checks if the event has been cancelled.
     *
     * @return {@code true} if the event is cancelled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}