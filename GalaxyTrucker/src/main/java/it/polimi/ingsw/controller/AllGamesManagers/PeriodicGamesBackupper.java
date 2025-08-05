package it.polimi.ingsw.controller.AllGamesManagers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeriodicGamesBackupper implements Runnable{
    private final int backupPeriodSeconds = 30;   // 30 seconds
    private final AllCurrentGames allCurrentGames;
    private final String backupFileName = "./GalaxyTrucker/resources/modelPeriodicBackup";    // relative path from the project root

    public PeriodicGamesBackupper(AllCurrentGames allCurrentGames){
        this.allCurrentGames = allCurrentGames;
    }

    public String getBackupFileName() { return backupFileName; }

    public void run(){
        try(
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)
        ) {
            scheduler.scheduleAtFixedRate(
                () -> {
                    try{
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(backupFileName));
                        out.writeObject(allCurrentGames.getAllGames());
                    }
                    catch (FileNotFoundException e){ System.out.println("Backup file not found"); }
                    catch (IOException e){ e.printStackTrace(System.out); }

                }, 30, backupPeriodSeconds, TimeUnit.SECONDS);
        }
        catch (IllegalArgumentException e){ System.out.println("Negative number of threads to handle backups"); }
    }
}
