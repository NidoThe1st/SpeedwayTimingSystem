package me.makkuusen.timing.system.api.events;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoatSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location location;
    private Boat boat = null;
    private boolean isPermitted = false;
    private boolean cancelled = false;

    public BoatSpawnEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    public Player getPlayer(){
        return player;
    }

    public Location getLocation(){
        return location;
    }

    public void setBoat(Boat boat){
        this.boat = boat;
    }

    public Boat getBoat() {
        return boat;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
