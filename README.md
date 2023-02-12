# Timing System


## What does it do?
TimingSystem is a plugin that aims to do a few things.
* Creation and timetrials of 3 various track types. [Boats, Elytra and Parkour]
* Displaying leaderboards. Currently both In-game holograms and a commandbased ones.
* Creating and managing events to host races. (Currently only supports Boats).

## Installation
This plugin has made it's first real release. If you want to use it, make sure to follow our installation guide on our [wiki](https://github.com/Makkuusen/TimingSystem/wiki/Installing-the-plugin) 

## Dependencies
1. World Edit (I do recommend the use of [FastAsyncWorldEdit](https://www.spigotmc.org/resources/fastasyncworldedit.13932/))
2. MYSQL Database. It needs a database connection. Configure it in the plugins config.yml.
3. [optional] [HolographicDisplays 3.0.0](https://dev.bukkit.org/projects/holographic-displays/files/4056176/download)

## Discord
If you need support. Look for #support in our discord.

http://discord.boatlabs.net

Timing System was originally forked from [EpicIceTrack](https://github.com/JustBru00/NetherCubeParkour).

## SpeedwayTimingSystem

This plugin is a fork from TimingSystem.

It is made for the Polish community of Speedway racers to better suit their style of running events.

## BASIC EVENT SETUP
1. /event create [name]

2. /event set track [track]

3. /round createmultiple [type] [number] - This will automatically make the [number] of rounds with heats in them

4. /heat set laps [number] [heat]

5. /heat set pits [number] [heat] - In speedway this is usually zero

6. /heat load [heat]

7. /heat start [heat] <countdown> - If put at 0, there will be no countdown
