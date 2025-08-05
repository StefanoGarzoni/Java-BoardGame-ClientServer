package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HourGlass implements Serializable {
    private int flippedTimes;
    private final long milliseconds;
    private boolean isFree;

    public HourGlass(long milliseconds){
        this.flippedTimes = 0;
        this.isFree = true;
        this.milliseconds = milliseconds;
    }

    public boolean isFree() {
        return isFree;
    }
    public void setNotFree() {
        isFree = false;
    }

    public void setFree(){
        isFree = true;
    }

    /** This method handles time management during Building phase. When the HourGlass' time is up. it notifies the event to the Controller
     */
    public void flip(){
        setNotFree();

        flippedTimes++;

        // scheduling of the hourglass expiration
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this::setFree, milliseconds, TimeUnit.MILLISECONDS);
    }

    public int getFlippedTimes(){
        return flippedTimes;
    }
}
