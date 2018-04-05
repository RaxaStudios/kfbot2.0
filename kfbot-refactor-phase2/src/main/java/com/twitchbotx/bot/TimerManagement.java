package com.twitchbotx.bot;

//import com.twitchbotx.gui.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.util.ArrayList;
import java.util.List;
//import java.io.PrintStream;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
//import org.w3c.dom.Element;

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

    public volatile boolean repeat;

    public void setupPeriodicBroadcast() {
        for (int i = 0; i < store.getCommands().size(); i++) {

            final ConfigParameters.Command command = guiHandler.bot.getStore().getCommands().get(i);
            //store.getCommands().get(i);
            final int index = i;
            if (Boolean.parseBoolean(command.repeating)) {
                int iDelay = Integer.parseInt(command.initialDelay);
                int interval = Integer.parseInt(command.interval);
                if (interval < 600) {
                    String event = "Repeating interval too short for command " + command.name;
                    Platform.runLater(() -> {
                        store.getEventList().addList(event);
                    });
/*
                    ses.schedule(new Runnable() {
                        Datastore store;

                        @Override
                        public void run() {
                            store = guiHandler.bot.getStore();
                            int interval = Integer.parseInt(store.getCommands().get(index).interval);
                            String content = store.getCommands().get(index).text;
                            boolean repeat = Boolean.parseBoolean(store.getCommands().get(index).repeating);
                            if (repeat) {
                                System.out.println("Testing repeating: interval:" + interval + " name: " + command.name + " content: " + content);
                                ses.schedule(this, interval, TimeUnit.SECONDS);
                                //ses.schedule(this, 10, TimeUnit.SECONDS);
                            } else {
                                //TODO start new runnable series for turning commands on to repeating while bot running
                                // end old editions of runnables to ensure no crossover
                                // fix issue when restarting bot through GUI restart button for repeating schedule 
                                System.out.println("Command " + command.name + " stopping repeat schedule");
                            }
                        }
                    }, 0, TimeUnit.SECONDS);*/
                } else {

                    ses.schedule(new Runnable() {
                        @Override
                        public void run() {

                            System.out.println("Testing repeating: interval:" + command.interval + " name: " + command.name + " content: " + command.text);
                            ses.schedule(this, Integer.parseInt(command.interval), TimeUnit.SECONDS);
                            //ses.schedule(this, 10, TimeUnit.SECONDS);
                        }
                    }, 0, TimeUnit.SECONDS);
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

    public void reschedule(String commandToSchedule) {
        for (int i = 0; i < store.getCommands().size(); i++) {

            final ConfigParameters.Command command = guiHandler.bot.getStore().getCommands().get(i);
            if (command.name.equals(commandToSchedule)) {
                int index = i;
                int iDelay = Integer.parseInt(command.initialDelay);
                int interval = Integer.parseInt(command.interval);
                if (interval < 600) {
                    String event = "Repeating interval too short for command " + command.name;
                    Platform.runLater(() -> {
                        store.getEventList().addList(event);
                    });
                } else {
                    ses.schedule(new Runnable() {
                        Datastore store;

                        @Override
                        public void run() {
                            store = guiHandler.bot.getStore();
                            int interval = Integer.parseInt(store.getCommands().get(index).interval);
                            String content = store.getCommands().get(index).text;
                            boolean repeat = Boolean.parseBoolean(store.getCommands().get(index).repeating);
                            if (repeat) {
                                System.out.println("Testing repeating: interval:" + interval + " name: " + command.name + " content: " + content);
                                ses.schedule(this, interval, TimeUnit.SECONDS);
                                //ses.schedule(this, 10, TimeUnit.SECONDS);
                            } else {
                                //TODO start new runnable series for turning commands on to repeating while bot running
                                // end old editions of runnables to ensure no crossover
                                // fix issue when restarting bot through GUI restart button for repeating schedule 
                                System.out.println("Command " + command.name + " stopping repeat schedule");
                            }
                        }
                    }, 0, TimeUnit.SECONDS);
                    //}, iDelay, interval);
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

    @ThreadSafe
    public static class repeatingCommandHandler {

        public repeatingCommandHandler() {

        }

        public void testRPC() {
            Runnable rTest = new Runnable() {
                @Override
                public void run() {

                    System.out.println("Testing repeating: interval:" + 7 + " name: " + 555 + " content: " + 999);
                    ses.schedule(this, Integer.parseInt("7"), TimeUnit.SECONDS);
                    //ses.schedule(this, 10, TimeUnit.SECONDS);
                }
            };
            List<Thread> threads = new ArrayList<>();
            Thread rT = new Thread(rTest);
            rT.setName("command name here");
            threads.add(rT);
            for (Thread tn : threads) {
                if (tn.getName().equals("command name here")) {
                    threads.remove(tn);
                }
            }
        }

    }

    public class T extends Thread {

        public void run() {
            System.out.println("Test 1");
        }
    }

}
