package com.twitchbotx.bot;

import com.twitchbotx.gui.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.w3c.dom.Element;

/*
** Roughly 15 minute intervals between command sendMessage, 
** OR based on number of messages between sendMessage
**
** RE-ADD online check timer foro online-only timer functionality 
** as well as for future discord stream is live function
 */
/**
 * This class is responsible for timer management.
 */
public final class TimerManagement {

    public static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    // private final PrintStream outstream;
    private final Datastore store;
    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    /*
** This takes the information parsed in the start of the program under 
** elements "repeating" and "interval"
** "repeating" = True commands need to start when the bot starts
** all commands set to repeat need to start (including created/edited commands)
     */
    public TimerManagement() {
        this.store = guiHandler.bot.getStore();
        //this.outstream = stream;
    }

    public void setupPeriodicBroadcast(final Datastore repeating) {
        for (int i = 0; i < store.getCommands().size(); i++) {

            final ConfigParameters.Command command = store.getCommands().get(i);

            if (Boolean.parseBoolean(command.repeating)) {
                int iDelay = Integer.parseInt(command.initialDelay);
                int interval = Integer.parseInt(command.interval);
                if (interval < 600) {
                    String event = "Repeating interval too short for command " + command.name;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            store.getEventList().addList(event);
                        }
                    });
                } else {
                    ses.scheduleWithFixedDelay(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(command.text);
                        }
                        // }, 0, 10, TimeUnit.SECONDS);
                    }, iDelay, interval, TimeUnit.SECONDS);
                    String event = "Starting repeating command: " + command.name;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            store.getEventList().addList(event);
                        }
                    });

                }
            }
        }
    }

    private void sendMessage(final String msg) {
        final String sendMessage = "/me > " + msg;
        guiHandler.bot.getOut().println("PRIVMSG #"
                + store.getConfiguration().joinedChannel
                + " "
                + ":"
                + sendMessage);
    }

    @ThreadSafe
    public static class pongHandler {

        @GuardedBy("this")
        private int pongCounter = 1;

        public synchronized int getPong() {
            return pongCounter;
        }

        public synchronized void addPong() {
            pongCounter++;
        }

        public synchronized void resetPong() {
            pongCounter = 0;
        }

    }

}
