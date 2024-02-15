package net.simplyvanilla.simplyvotifier.model;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * {@code VotifierEvent} is a custom Bukkit event class that is sent
 * synchronously to CraftBukkit's main thread allowing other plugins to listener
 * for votes.
 *
 * @author frelling
 */
@Getter
public class VotifierEvent extends Event {
    /**
     * Event listener handler list.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Encapsulated vote record.
     */
    private Vote vote;

    /**
     * Constructs a vote event that encapsulated the given vote record.
     *
     * @param vote vote record
     */
    public VotifierEvent(final Vote vote) {
        super(!Bukkit.isPrimaryThread());
        this.vote = vote;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
