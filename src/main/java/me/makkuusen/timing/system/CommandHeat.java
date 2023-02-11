package me.makkuusen.timing.system;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.event.Event;
import me.makkuusen.timing.system.event.EventAnnouncements;
import me.makkuusen.timing.system.event.EventDatabase;
import me.makkuusen.timing.system.event.EventResults;
import me.makkuusen.timing.system.heat.Heat;
import me.makkuusen.timing.system.heat.HeatState;
import me.makkuusen.timing.system.heat.Lap;
import me.makkuusen.timing.system.participant.Driver;
import me.makkuusen.timing.system.round.FinalRound;
import me.makkuusen.timing.system.round.QualificationRound;
import me.makkuusen.timing.system.round.Round;
import me.makkuusen.timing.system.timetrial.TimeTrialFinish;
import me.makkuusen.timing.system.timetrial.TimeTrialFinishComparator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@CommandAlias("heat")
public class CommandHeat extends BaseCommand {

    @Default
    @Subcommand("list")
    public static void onHeats(Player player, @Optional Event event) {
        if (event == null) {
            var maybeEvent = EventDatabase.getPlayerSelectedEvent(player.getUniqueId());
            if (maybeEvent.isPresent()) {
                event = maybeEvent.get();
            } else {
                player.sendMessage("§cYou have no event selected");
                return;
            }
        }
        var messages = event.eventSchedule.getHeatList(event);
        messages.forEach(message -> player.sendMessage(message));
        return;
    }

    @Subcommand("info")
    @CommandCompletion("@heat")
    public static void onHeatInfo(Player player, Heat heat) {
        player.sendMessage("§2Heat: §a" + heat.getName());
        player.sendMessage("§2Heatstate: §a" + heat.getHeatState().name());
        if (heat.getTimeLimit() != null) {
            player.sendMessage("§2TimeLimit: §a" + (heat.getTimeLimit() / 1000) + "s");
        }
        if (heat.getStartDelay() != null) {
            player.sendMessage("§2StartDelay: §a" + (heat.getStartDelay()) + "ms");
        }

        if (heat.getTotalLaps() != null) {
            player.sendMessage("§2Laps: §a" + heat.getTotalLaps());
        }
        if (heat.getTotalPits() != null) {
            player.sendMessage("§2Pits: §a" + heat.getTotalPits());
        }
        if (heat.getFastestLapUUID() != null) {
            Driver d = heat.getDrivers().get(heat.getFastestLapUUID());
            player.sendMessage("§2Fastest lap: §a" + ApiUtilities.formatAsTime(d.getBestLap().get().getLapTime()) + " §2by §a" + d.getTPlayer().getName());
        }
        player.sendMessage("§2MaxDrivers: §a" + heat.getMaxDrivers());
        player.sendMessage("§2Drivers:");
        for (Driver d : heat.getStartPositions()) {
            player.sendMessage("  " + d.getStartPosition() + ": "+ d.getTPlayer().getName());
        }
    }

    @Subcommand("start")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatStart(CommandSender sender, Heat heat, @Optional Event event, @Optional Integer countdown) {
        if (countdown == null){
            countdown = 5;
        }

        if (sender instanceof ConsoleCommandSender){

        }

        if (heat.startCountdown(countdown)) {
            sender.sendMessage("§aStarted countdown for " + heat.getName());
            return;
        }
        sender.sendMessage("§cCouldn't start " + heat.getName());
    }

    @Subcommand("finish")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatFinish(CommandSender sender, Heat heat) {
        if (heat.finishHeat()) {
            sender.sendMessage("§aFinished " + heat.getName());
            return;
        }
        sender.sendMessage("§cCouldn't finish " + heat.getName());
        return;
    }

    @Subcommand("load")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatLoad(CommandSender sender, Heat heat) {
        if (heat.loadHeat()) {
            EventAnnouncements.broadcastSpectate(heat.getEvent());
            sender.sendMessage("§aLoaded " + heat.getName());
            return;
        }
        sender.sendMessage("§cCouldn't load " + heat.getName());

    }

    @Subcommand("reload")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatReload(CommandSender sender, Heat heat) {
        if (heat.resetHeat()) {
            if (heat.loadHeat()) {
                sender.sendMessage("§aReloaded " + heat.getName());
                return;
            }
            sender.sendMessage("§cCouldn't load " + heat.getName());
            return;
        }
        sender.sendMessage("§cCouldn't reset " + heat.getName());
    }

    @Subcommand("reset")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatReset(CommandSender sender, Heat heat) {
        if (heat.resetHeat()){
            EventAnnouncements.broadcastReset(heat);
            sender.sendMessage("§aReset " + heat.getName());
            return;
        }
        sender.sendMessage("§cCould not reset " + heat.getName());
        return;
    }

    @Subcommand("delete")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatRemove(Player player, Heat heat) {
        if (EventDatabase.removeHeat(heat)){
            player.sendMessage("§aHeat was removed");
            return;
        }
        player.sendMessage("§cHeat could not be removed. Is the event already finished?");
    }

    @Subcommand("create")
    @CommandCompletion("@round")
    @CommandPermission("event.admin")
    public static void onHeatCreate(Player player, Round round, @Optional Event event){
        if (event == null) {
            var maybeEvent = EventDatabase.getPlayerSelectedEvent(player.getUniqueId());
            if (maybeEvent.isPresent()) {
                event = maybeEvent.get();
            } else {
                player.sendMessage("§cYou have no event selected, /event select <name>");
                return;
            }
        }
        if (event.getTrack() == null) {
            player.sendMessage("§cYour event needs a track, /event set track <name>");
            return;
        }
        round.createHeat(round.getHeats().size() + 1);
        player.sendMessage("§aCreated heat for " + round.getDisplayName());
    }

    @Subcommand("createmultiple")
    @CommandCompletion("@round <number>")
    @CommandPermission("event.admin")
    public static void onCreateMultipleHeats(Player player, Round round,Integer heatNumber, @Optional Event event){

        if (event == null) {
            var maybeEvent = EventDatabase.getPlayerSelectedEvent(player.getUniqueId());
            if (maybeEvent.isPresent()) {
                event = maybeEvent.get();
            } else {
                player.sendMessage("§cYou have no event selected, /event select <name>");
                return;
            }
        }
        if (event.getTrack() == null) {
            player.sendMessage("§cYour event needs a track, /event set track <name>");
            return;
        }
        AtomicInteger i = new AtomicInteger(0);
        while (i.getAndAdd(1) < heatNumber){
            round.createHeat(round.getHeats().size() + 1);
        }
        player.sendMessage("§aCreated " + heatNumber + " heats for " + round.getDisplayName());
    }

    @Subcommand("createpackage")
    @CommandCompletion("<number>")
    @CommandPermission("event.admin")
    public static void onCreatePackage(Player player, Integer numberOfPackages, @Optional Event event){
        if (event == null) {
            var maybeEvent = EventDatabase.getPlayerSelectedEvent(player.getUniqueId());
            if (maybeEvent.isPresent()) {
                event = maybeEvent.get();
            } else {
                player.sendMessage("§cYou have no event selected, /event select <name>");
                return;
            }
        }
        if (event.getTrack() == null) {
            player.sendMessage("§cYour event needs a track, /event set track <name>");
            return;
        }

        AtomicInteger i = new AtomicInteger(0);
        while (i.getAndAdd(1) < numberOfPackages){

        }
    }

    @Subcommand("set laps")
    @CommandPermission("event.admin")
    @CommandCompletion("<laps> @heat")
    public static void onHeatSetLaps(Player player, Integer laps, Heat heat) {
        heat.setTotalLaps(laps);
        player.sendMessage("§aLaps has been updated");
    }

    @Subcommand("set pits")
    @CommandPermission("event.admin")
    @CommandCompletion("<pits> @heat")
    public static void onHeatSetPits(Player player, Integer pits, Heat heat) {
        if (heat.getRound() instanceof QualificationRound) {
            player.sendMessage("§cYou can only modify total pits of a final heat.");
            return;
        } else {
            heat.setTotalPits(pits);
            player.sendMessage("§aPits has been updated");
        }
    }

    @Subcommand("set startdelay")
    @CommandPermission("event.admin")
    @CommandCompletion("<h/m/s> @heat")
    public static void onHeatStartDelay(Player player, String startDelay, Heat heat) {
        Integer delay = ApiUtilities.parseDurationToMillis(startDelay);
        if (delay == null){
            player.sendMessage("§cYou need to format the time correctly, e.g. 2s");
            return;
        }
        heat.setStartDelayInTicks(delay);
        player.sendMessage("§aStart delay has been updated");

    }

    @Subcommand("set timelimit")
    @CommandPermission("event.admin")
    @CommandCompletion("<h/m/s> @heat")
    public static void onHeatSetTime(Player player, String time, Heat heat) {
        Integer timeLimit = ApiUtilities.parseDurationToMillis(time);
        if (timeLimit == null){
            player.sendMessage("§cYou need to format the time correctly, e.g. 2m");
            return;
        }
        heat.setTimeLimit(timeLimit);
        player.sendMessage("§aTime limit has been updated");
    }

    @Subcommand("set maxdrivers")
    @CommandPermission("event.admin")
    @CommandCompletion("<max> @heat")
    public static void onHeatMaxDrivers(Player player, Integer maxDrivers, Heat heat) {
        heat.setMaxDrivers(maxDrivers);
        player.sendMessage("§aMax drivers has been updated");
    }

    @Subcommand("set driverposition")
    @CommandPermission("event.admin")
    @CommandCompletion("@players <[+/-]pos> @heat")
    public static void onHeatSetDriverPosition(Player sender, String playerName, String position, Heat heat){
        TPlayer tPlayer = Database.getPlayer(playerName);
        if (tPlayer == null) {
            sender.sendMessage("§cCould not find player");
            return;
        }
        if (heat.getDrivers().get(tPlayer.getUniqueId()) == null) {
            sender.sendMessage("§cPlayer is not in heat!");
            return;
        }
        Driver driver = heat.getDrivers().get(tPlayer.getUniqueId());
        if (heat.isRacing()) {
            sender.sendMessage("§cHeat is currently running");
            return;
        }
        if (getParsedIndex(position) == null) {
            TimingSystem.getPlugin().sendMessage(sender, "messages.error.numberException");
            return;
        }
        int pos;
        if (getParsedRemoveFlag(position)) {
            pos = driver.getStartPosition() - getParsedIndex(position);
        } else if (getParsedAddFlag(position)) {
            pos = driver.getStartPosition() + getParsedIndex(position);
        } else {
            pos = getParsedIndex(position);
        }

        if (pos > heat.getDrivers().size()) {
            sender.sendMessage("§cYou can't start further back than there are drivers");
            return;
        }

        if (pos < 1) {
            sender.sendMessage("§cYou can't start further forward than 1");
            return;
        }

        if (pos == driver.getStartPosition()) {
            sender.sendMessage("§cYou can't move the driver to the same position");
            return;
        }


        if (heat.setDriverPosition(driver, pos)) {
            sender.sendMessage("§a" + driver.getTPlayer().getName() + " is now starting " + pos );
            if (heat.getHeatState() == HeatState.LOADED) {
                heat.reloadHeat();
            }
            return;
        }
        sender.sendMessage("§cCould not change position of driver");

    }

    @Subcommand("set reversegrid")
    @CommandCompletion("@heat <%>")
    public static void onReverseGrid(Player player, Heat heat, @Optional Integer percentage){
        if (percentage == null) {
            percentage = 100;
        }
        heat.reverseGrid(percentage);
        if (heat.getHeatState() == HeatState.LOADED) {
            heat.reloadHeat();
        }
        player.sendMessage("§aReversed the first "+ percentage +"% of the grid");
    }

    @Subcommand("add")
    @CommandPermission("event.admin")
    @CommandCompletion("@players @heat")
    public static void onHeatAddDriver(Player sender, String playerName, Heat heat) {
        if (heat.getRound().getRoundIndex() != heat.getEvent().getEventSchedule().getCurrentRound() && heat.getRound().getRoundIndex() != 1){
            sender.sendMessage("§cYou can't add driver to a future round before the current round has finished");
            return;
        }

        if (heat.getMaxDrivers() <= heat.getDrivers().size()) {
            sender.sendMessage("§cMax allowed amount of drivers have been added");
            return;
        }
        TPlayer tPlayer = Database.getPlayer(playerName);
        if (tPlayer == null) {
            sender.sendMessage("§cCould not find player");
            return;
        }

        for (Heat h : heat.getRound().getHeats()) {
            if (h.getDrivers().get(tPlayer.getUniqueId()) != null) {
                sender.sendMessage("§cPlayer is already in this round!");
                return;
            }
        }

        if (EventDatabase.heatDriverNew(tPlayer.getUniqueId(), heat, heat.getDrivers().size() + 1)) {
            sender.sendMessage("§aAdded driver");
            if (heat.getHeatState() == HeatState.LOADED) {
                heat.reloadHeat();
            }
            return;
        }

        sender.sendMessage("§cCould not add driver to heat");
    }



    @Subcommand("delete driver")
    @CommandPermission("event.admin")
    @CommandCompletion("@players @heat")
    public static void onHeatRemoveDriver(Player sender, String playerName, Heat heat){
        TPlayer tPlayer = Database.getPlayer(playerName);
        if (tPlayer == null) {
            sender.sendMessage("§cCould not find player");
            return;
        }
        if (heat.getDrivers().get(tPlayer.getUniqueId()) == null) {
            sender.sendMessage("§cPlayer is not in heat!");
            return;
        }
        if (heat.isRacing()) {
            if (heat.disqualifyDriver(heat.getDrivers().get(tPlayer.getUniqueId()))) {
                if (tPlayer.getPlayer() != null) {
                    if (tPlayer.getPlayer().getVehicle() != null && tPlayer.getPlayer().getVehicle() instanceof Boat boat) {
                        boat.remove();
                    }
                    Location loc = tPlayer.getPlayer().getBedSpawnLocation() == null ? tPlayer.getPlayer().getWorld().getSpawnLocation() : tPlayer.getPlayer().getBedSpawnLocation();
                    tPlayer.getPlayer().teleport(loc);
                }
                sender.sendMessage("§aDriver has been disqualifed");
                return;
            }
            sender.sendMessage("§cDriver could not be disqualified");
        } else {
            boolean reload = false;
            if (heat.getHeatState() == HeatState.LOADED) {
                heat.resetHeat();
                reload = true;
            }
            if (heat.removeDriver(heat.getDrivers().get(tPlayer.getUniqueId()))) {
                boolean removeSpectator = true;
                for (Round round : heat.getEvent().getEventSchedule().getRounds()) {
                    for (Heat h : round.getHeats()) {
                        if (h.getDrivers().containsKey(tPlayer.getUniqueId())) {
                            removeSpectator = false;
                        }
                    }
                }
                if (removeSpectator) {
                    heat.getEvent().removeSpectator(tPlayer.getUniqueId());
                }
                sender.sendMessage("§aDriver has been removed");
                if (reload) {
                    heat.loadHeat();
                }
                return;
            }
            sender.sendMessage("§cDriver could not be removed");
        }
    }
    @Subcommand("quit")
    public static void onHeatDriverQuit(Player player) {
        if (EventDatabase.getDriverFromRunningHeat(player.getUniqueId()).isEmpty()) {
            player.sendMessage("§cYou are not in a running heat!");
            return;
        }
        Driver driver = EventDatabase.getDriverFromRunningHeat(player.getUniqueId()).get();
        if (driver.getHeat().disqualifyDriver(driver)) {
            if (player.getVehicle() != null && player.getVehicle() instanceof Boat boat) {
                boat.remove();
            }
            Location loc = player.getBedSpawnLocation() == null ? player.getWorld().getSpawnLocation() : player.getBedSpawnLocation();
            player.teleport(loc);
            player.sendMessage("§aYou have aborted the heat");
            return;
        }
        player.sendMessage("§cYou could not abort the event.");
    }


    @Subcommand("add alldrivers")
    @CommandPermission("event.admin")
    @CommandCompletion("@heat")
    public static void onHeatAddDrivers(Player sender, Heat heat) {
        if (heat.getRound().getRoundIndex() != heat.getEvent().getEventSchedule().getCurrentRound() && heat.getRound().getRoundIndex() != 1){
            sender.sendMessage("§cYou can't add drivers to a future round before the current round has finished");
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (heat.getMaxDrivers() <= heat.getDrivers().size()) {
                sender.sendMessage("§cMax allowed amount of drivers have been added");
                return;
            }
            for (Heat h : heat.getRound().getHeats()) {
                if (h.getDrivers().get(player.getUniqueId()) != null) {
                    continue;
                }
            }
            if (heat.getDrivers().get(player.getUniqueId()) != null) {
                continue;
            }
            if (EventDatabase.heatDriverNew(player.getUniqueId(), heat, heat.getDrivers().size() + 1)) {
                continue;
            }
            if (heat.getHeatState() == HeatState.LOADED) {
                heat.reloadHeat();
            }
        }
        sender.sendMessage("§aAll online players has been added");
    }



    @Subcommand("results")
    @CommandCompletion("@heat @players")
    public static void onHeatResults(Player sender, Heat heat, @Optional String name) {

        if (name != null) {
            TPlayer tPlayer = Database.getPlayer(name);
            if (tPlayer == null) {
                sender.sendMessage("§cCould not find player");
                return;
            }
            if (heat.getDrivers().get(tPlayer.getUniqueId()) == null) {
                sender.sendMessage("§cPlayer is not in heat!");
                return;
            }
            Driver driver = heat.getDrivers().get(tPlayer.getUniqueId());
            sender.sendMessage("§2Results for §a" + tPlayer.getNameDisplay() + "§2 in §a" + heat.getName());
            sender.sendMessage("§2Position: §a" + driver.getPosition());
            sender.sendMessage("§2StartPosition: §a" + driver.getStartPosition());
            var maybeBestLap = driver.getBestLap();
            if (maybeBestLap.isPresent()){
                sender.sendMessage("§2Fastest Lap: §a" + ApiUtilities.formatAsTime(maybeBestLap.get().getLapTime()));
            }
            int count = 1;
            for (Lap l : driver.getLaps()) {
                String lap = "§7Lap " + count + ": §f" + ApiUtilities.formatAsTime(l.getLapTime());
                if (l.equals(maybeBestLap.get())) {
                    lap += " §7(F)";
                }
                if (l.isPitted()) {
                    lap += " §7(P)";
                }
                sender.sendMessage(lap);
                count++;
            }
            return;
        }
        if (heat.getHeatState() == HeatState.FINISHED) {

            sender.sendMessage("§2Results for heat §a" + heat.getName());
            if (heat.getFastestLapUUID() != null) {
                Driver d = heat.getDrivers().get(heat.getFastestLapUUID());
                sender.sendMessage("§2Fastest lap: §a" + ApiUtilities.formatAsTime(d.getBestLap().get().getLapTime()) + " §2by §a" + d.getTPlayer().getName());
            }
            List<Driver> result = EventResults.generateHeatResults(heat);
            if (heat.getRound() instanceof FinalRound){
                for (Driver d : result) {
                    sender.sendMessage("§2" + d.getPosition() + ". §a" + d.getTPlayer().getName() + "§2 - §a" + d.getLaps().size() + " §2laps in §a" + ApiUtilities.formatAsTime(d.getFinishTime()));
                }
            } else {
                for (Driver d : result) {
                    sender.sendMessage("§2" + d.getPosition() + ". §a" + d.getTPlayer().getName() + "§2 - §a"  + (d.getBestLap().isPresent() ? ApiUtilities.formatAsTime(d.getBestLap().get().getLapTime()) : "0"));
                }
            }
        } else {
            sender.sendMessage("§cHeat has not been finished");
        }
    }

    @Subcommand("sort tt")
    @CommandCompletion("@heat")
    @CommandPermission("event.admin")
    public static void onSortByTT(Player player, Heat heat) {
        if (heat.getHeatState() == HeatState.FINISHED) {
            player.sendMessage("§cYou cannot sort an finished started heat");
            return;
        }
        if (heat.isRacing()) {
            player.sendMessage("§cYou cannot sort an already started heat");
            return;
        }
        if (heat.getStartPositions().size() == 0) {
            player.sendMessage("§aNo drivers to sort");
            return;
        }
        if (heat.getRound().getRoundIndex() != heat.getEvent().getEventSchedule().getCurrentRound() && heat.getRound().getRoundIndex() != 1) {
            player.sendMessage("§cYou can't sort drivers in a future round before the current round has finished");
            return;
        }

        List<TimeTrialFinish> driversWithBestTimes = heat.getEvent().getTrack().getTopList().stream().filter(tt -> heat.getDrivers().keySet().contains(tt.getPlayer().getUniqueId())).collect(Collectors.toList());
        List<Driver> allDrivers = new ArrayList<>();
        allDrivers.addAll(heat.getStartPositions());
        List<Driver> noTT = new ArrayList<>();

        int i = 1;
        for (Driver driver : allDrivers)  {
            boolean match = false;
            for (TimeTrialFinish finish : driversWithBestTimes)  {
                if (finish.getPlayer() == driver.getTPlayer()) {
                    heat.setDriverPosition(driver, driversWithBestTimes.indexOf(finish) + 1);
                    i++;
                    match = true;
                    break;
                }
            }
            if (match == false){
                noTT.add(driver);
            }
        }

        for (Driver driver : noTT) {
            heat.setDriverPosition(driver, i);
            i++;
        }

        if (heat.getHeatState() == HeatState.LOADED) {
            heat.reloadHeat();
        }

        player.sendMessage("§aThe heat has been sorted by fastest times");
    }

    @Subcommand("sort random")
    @CommandCompletion("@heat")
    @CommandPermission("event.admin")
    public static void onSortByRandom(Player player, Heat heat) {
        if (heat.getHeatState() == HeatState.FINISHED) {
            player.sendMessage("§cYou cannot sort an finished started heat");
            return;
        }
        if (heat.isRacing()) {
            player.sendMessage("§cYou cannot sort an already started heat");
            return;
        }
        if(heat.getStartPositions().size() == 0) {
            player.sendMessage("§aNo drivers to sort");
            return;
        }
        if (heat.getRound().getRoundIndex() != heat.getEvent().getEventSchedule().getCurrentRound() && heat.getRound().getRoundIndex() != 1) {
            player.sendMessage("§cYou can't sort drivers in a future round before the current round has finished");
            return;
        }

        List<Driver> randomDrivers = new ArrayList<>();
        randomDrivers.addAll(heat.getStartPositions());
        Collections.shuffle(randomDrivers);

        for (int i = 0; i < randomDrivers.size(); i++) {
            heat.setDriverPosition(randomDrivers.get(i), i + 1);
        }

        if (heat.getHeatState() == HeatState.LOADED) {
            heat.reloadHeat();
        }

        player.sendMessage("§aThe heat has been sorted randomly");
    }

    private static boolean getParsedRemoveFlag(String index) {
        return index.startsWith("-");
    }

    private static boolean getParsedAddFlag(String index) {
        return index.startsWith("+");
    }

    private static Integer getParsedIndex(String index) {
        if (index.startsWith("-")) {
            index = index.substring(1);
        } else if (index.startsWith("+")) {
            index = index.substring(1);
        }
        try {
            return Integer.parseInt(index);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}

