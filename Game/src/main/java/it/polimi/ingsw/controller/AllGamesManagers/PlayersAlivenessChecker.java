package it.polimi.ingsw.controller.AllGamesManagers;

import it.polimi.ingsw.ActionMessage;
import it.polimi.ingsw.model.ModelPublisher;

import java.util.List;

/** Threads that, every pingPeriodMilliseconds, implements a ping-pong communication to verify the aliveness of all players.
 * If the client is alive, will reply "PONG"
 */
public class PlayersAlivenessChecker extends Thread{
    private final int pingPeriodMilliseconds = 30000;   // 30 seconds
    private final AllCurrentGames allCurrentGames;

    PlayersAlivenessChecker(AllCurrentGames allCurrentGames){
        setDaemon(true);
        this.allCurrentGames = allCurrentGames;
    }

    /** Manages all players connection aliveness checks.
     * Periodically (30 seconds) a thread will send a PING message to all clients, that must respond with a PONG message.
     * After few seconds, the ModelPublisher::removeNotAlivePlayers will be invoked to remove inactive players.
     *
     */
    @Override
    public void run() {
        List<ModelPublisher> allGamesPublishers;
        while(true){
            if(!(allGamesPublishers = allCurrentGames.getAllGamesPublishers()).isEmpty()) {
                allGamesPublishers.forEach(ModelPublisher::resetPlayerAliveness);
                allGamesPublishers.forEach((publisher) -> publisher.notify(new ActionMessage("PING", "server")));

                try { Thread.sleep(pingPeriodMilliseconds); }
                catch (InterruptedException e) { e.printStackTrace(System.out); }

                // always asks for an updated list of Games' Publishers, to avoid access to no longer existing publishers
                allCurrentGames.getAllGamesPublishers().forEach(ModelPublisher::removeNotAlivePlayers);
            }
        }
    }
}
