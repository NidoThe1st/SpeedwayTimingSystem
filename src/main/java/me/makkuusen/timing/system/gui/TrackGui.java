package me.makkuusen.timing.system.gui;

import me.makkuusen.timing.system.ApiUtilities;
import me.makkuusen.timing.system.TPlayer;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.TrackDatabase;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrackGui extends TrackPageGui{

    public TrackGui(TPlayer tPlayer, int page) {
        super(tPlayer, "§2§lTracks", 6, page);
    }

    public TrackGui(TPlayer tPlayer, int page, TrackSort trackSort) {
        super(tPlayer, "§2§lTracks", 6, page, trackSort);
    }

    @Override
    public GuiButton getPageButton(ItemStack item, TPlayer tPlayer, int page){
        var button = new GuiButton(item);
        button.setAction(() -> {
            new TrackGui(tPlayer, page, trackSort).show(tPlayer.getPlayer());
        });
        return button;
    }

    public List<Track> getTracks(int page, TrackSort trackSort) {
        List<Track> tracks;
        if (page == ELYTRAPAGE) {
            tracks = TrackDatabase.getTracks().stream().filter(Track::isElytraTrack).collect(Collectors.toList());
            sortTracks(tracks, trackSort);
        } else if (page == PARKOURPAGE) {
            tracks = TrackDatabase.getTracks().stream().filter(Track::isParkourTrack).collect(Collectors.toList());
            sortTracks(tracks, trackSort);
        } else {
            List<Track> tempTracks = TrackDatabase.getTracks().stream().filter(Track::isBoatTrack).collect(Collectors.toList());
            sortTracks(tempTracks, trackSort);
            int start = 36 * page;
            tracks = new ArrayList<>();
            for (int i = start; i < Math.min(start + 36, tempTracks.size()); i++) {
                tracks.add(tempTracks.get(i));
            }
        }
        return tracks;
    }

    @Override
    public GuiButton getTrackButton(Player player, Track track){
        var item = setTrackLore(track, track.getGuiItem(player.getUniqueId()));
        var button = new GuiButton(item);
        button.setAction(() -> {
            if (!track.getSpawnLocation().isWorldLoaded()) {
                player.sendMessage("§cWorld is not loaded!");
                return;
            }
            player.teleport(track.getSpawnLocation());
            player.closeInventory();
        });
        return button;
    }

    @Override
    public GuiButton getSortingButton(ItemStack item, TPlayer tPlayer, int page, TrackSort trackSort) {
        var button = new GuiButton(item);
        button.setAction(() -> {
            new TrackGui(tPlayer, page, trackSort).show(tPlayer.getPlayer());
        });
        return button;
    }

    private ItemStack setTrackLore(Track track, ItemStack toReturn){
        List<Component> loreToSet = new ArrayList<>();
        loreToSet.add(Component.text("§7Total Finishes: §e" + track.getTotalFinishes()));
        loreToSet.add(Component.text("§7Total Attempts: §e" + (track.getTotalFinishes() + track.getTotalAttempts())));
        loreToSet.add(Component.text("§7Time Spent: §e" + ApiUtilities.formatAsTimeSpent(track.getTotalTimeSpent())));
        loreToSet.add(Component.text("§7Created by: §e" + track.getOwner().getName()));
        loreToSet.add(Component.text("§7Created at: §e" + ApiUtilities.niceDate(track.getDateCreated())));

        ItemMeta im = toReturn.getItemMeta();
        im.lore(loreToSet);
        toReturn.setItemMeta(im);
        return toReturn;
    }
}
