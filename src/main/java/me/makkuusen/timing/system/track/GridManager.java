package me.makkuusen.timing.system.track;

import me.makkuusen.timing.system.ApiUtilities;
import me.makkuusen.timing.system.Database;
import me.makkuusen.timing.system.TimingSystem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class GridManager {

    public GridManager() {
    }

    public void teleportPlayerToGrid(Player player, Location location){
        location.setPitch(player.getLocation().getPitch());
        if (!location.isWorldLoaded()) {
            return;
        }
        if (player.getVehicle() != null && player.getVehicle() instanceof Boat boat) {
            boat.remove();
        }
        player.teleport(location);
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        Bukkit.getScheduler().runTaskLater(TimingSystem.getPlugin(), () -> {
            Boat boat = ApiUtilities.spawnBoatAndAddPlayer(player, location);
        }, 2);
    }

    public void startPlayerFromGrid(UUID uuid) {
    }

    public void clearArmorstands(){
    }
}
