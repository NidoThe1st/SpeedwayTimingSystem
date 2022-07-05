package me.makkuusen.timing.system;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class RaceLap {

    private RaceDriver raceDriver;
    private Track track;
    private Instant lapEnd;
    private Instant lapStart;
    private ArrayList<Instant> checkpoints = new ArrayList<>();

    public RaceLap(RaceDriver raceDriver) {
        this.raceDriver = raceDriver;
        this.track = raceDriver.race.getTrack();
    }

    public void setCheckpoint(int checkpoint, Instant timeStamp)
    {
        checkpoints.set(checkpoint, timeStamp);
    }

    public void setLapEnd(Instant timeStamp){
        lapEnd = timeStamp;
    }

    public void setLapStart(Instant timeStamp){
        lapStart = timeStamp;
    }

    public Instant getLapStart(){
        return lapStart;
    }

    public long getLaptime(){

        return Duration.between(lapStart, lapEnd).toMillis();
    }

    public RaceDriver getRaceDriver() {
        return raceDriver;
    }

    public int getLatestCheckpoint()
    {
        return checkpoints.size();
    }

    public int getNextCheckpoint()
    {
        if(track.getCheckpoints().size() >= checkpoints.size())
        {
            return checkpoints.size() + 1;
        }
        return checkpoints.size();
    }

    public boolean hasPassedAllCheckpoints()
    {
        return checkpoints.size() == track.getCheckpoints().size();
    }

    public void passNextCheckpoint(Instant timeStamp)
    {
        checkpoints.add(timeStamp);
    }

    public Instant getPassedCheckpointTime(int checkpoint){
        return checkpoints.get(checkpoint - 1);
    }
}
