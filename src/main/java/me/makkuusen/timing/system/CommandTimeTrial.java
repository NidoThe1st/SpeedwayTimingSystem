package me.makkuusen.timing.system;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.makkuusen.timing.system.gui.TimeTrialGui;
import me.makkuusen.timing.system.timetrial.TimeTrialController;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

import java.util.Random;

@CommandAlias("timetrial|tt")
public class CommandTimeTrial extends BaseCommand {
    static TimingSystem plugin;

    @Default
    @CommandCompletion("@track")
    public static void onTimeTrial(Player player, @Optional Track track) {
        if (track == null) {
            var tPlayer = Database.getPlayer(player.getUniqueId());
            new TimeTrialGui(tPlayer, 0).show(player);
        } else {
            if (!track.getSpawnLocation().isWorldLoaded()) {
                player.sendMessage("§cWorld is not loaded!");
                return;
            }

            if (!track.isOpen() && !(player.isOp() || player.hasPermission("track.admin"))) {
                player.sendMessage("§cTrack is closed!");
                return;
            }
            ApiUtilities.teleportPlayerAndSpawnBoat(player,track.isBoatTrack(), track.getSpawnLocation());
        }
    }

    @Subcommand("cancel|c")
    public static void onCancel(Player player) {
        if (!TimeTrialController.timeTrials.containsKey(player.getUniqueId())) {
            plugin.sendMessage(player, "messages.error.runNotStarted");
            return;
        }
        TimeTrialController.playerCancelMap(player);
        plugin.sendMessage(player, "messages.cancel");
    }

    @Subcommand("random|r")
    public static void onRandom(Player player){
        if(TrackDatabase.getOpenTracks().isEmpty()){
            plugin.sendMessage(player, "messages.randomTrack.noTracks");
            return;
        }

        Track t = TrackDatabase.getOpenTracks().get(new Random().nextInt(TrackDatabase.getOpenTracks().size()));

        if (!t.getSpawnLocation().isWorldLoaded()) {
            player.sendMessage("§cWorld is not loaded!");
            return;
        }

        if(t.getPlayerTopListPosition(Database.getPlayer(player.getUniqueId())) != -1){
            plugin.sendMessage(player, "messages.randomTrack.teleport", "%track%", t.getDisplayName(),
                    "%pos%", String.valueOf(t.getPlayerTopListPosition(Database.getPlayer(player.getUniqueId())))
            );
        } else {
            plugin.sendMessage(player, "messages.randomTrack.teleportNoPos", "%track%", t.getDisplayName());
        }

        ApiUtilities.teleportPlayerAndSpawnBoat(player, t.isBoatTrack(), t.getSpawnLocation());
    }
}