package com.twitchbotx.bot;

//import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.bot.TimerManagement.Task;
import com.twitchbotx.bot.handlers.CommonUtility;
import com.twitchbotx.bot.handlers.DonationHandler;
import com.twitchbotx.bot.handlers.PubSubBitsHandler;
import com.twitchbotx.bot.handlers.PubSubSubscriptionHandler;
import com.twitchbotx.bot.handlers.WhisperHandler;
import com.twitchbotx.gui.DashboardController;
import static com.twitchbotx.gui.DashboardController.eventObL;
import com.twitchbotx.gui.guiHandler;
//import com.twitchbotx.gui.SpoopathonController;
//import com.twitchbotx.gui.guiHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the application for a Twitch Bot.
 */
public final class TwitchBotX {

    private Logger LOGGER;
    private static volatile boolean cancelled = false;
    private final Datastore store;
    private PrintStream out;
    private BufferedReader in;
    private Socket socket;
    private String dataIn;
    private int reconCount = 0;
    private static final String BOT_VERSION = "v2.00";
    private DashboardController dc;
    private ScheduledExecutorService sPP;
    private ScheduledExecutorService timedManagement;
    public TimerManagement timers;
    public static TimerManagement.pongHandler pH = new TimerManagement.pongHandler();
    private final ConfigParameters configuration = new ConfigParameters();

    public TwitchBotX(Datastore getStore, BufferedReader getIn, PrintStream getOut, Socket getSocket) {
        this.dc = new DashboardController();
        this.store = getStore;
        this.out = getOut;
        this.in = getIn;
        this.socket = getSocket;
        LOGGER = CommonUtility.ERRORLOGGER;
    }

    public Socket getSock() {
        return this.socket;
    }

    public Datastore getStore() {
        return this.store;
    }

    public PrintStream getOut() {
        return this.out;
    }

    public BufferedReader getIn() {
        return this.in;
    }

    /**
     * This method will begin reading for incoming messages from Twitch IRC API.
     *
     * @param store The database utility for accessing and updating.
     */
    public void beginReadingMessages() {

        final CommandParser parser = new CommandParser(store, out);
        try {
            while (!cancelled) {
                dataIn = store.getBot().in.readLine();
                parser.parse(dataIn);
            }
            System.out.println("Bot shutting down");
            throw new NullPointerException();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error occurred with I/O in beginReadingMessages, perhaps with the Twitch API: {0}", e.toString());
            ///reconCount++;
            e.printStackTrace();
            store.getBot().reconnect();
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, "Shutting down bot");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A general error occurred parsing the message: {0}", e.toString());
        }
    }

    //method to stop reading loop
    public void cancel() {
        cancelled = true;
        out.println("ping");
        //clear out old timers before
        for (Entry<String, Task> fr : timers.getLHM().entrySet()) {
            fr.getValue().shutdown();
            timers.getLHM().remove(fr.getKey());
        }
        out = null;
        in = null;
    }

    /**
     * This method will attempt to reconnect to a twitch
     *
     * If it fails, it will throw an IOException.
     *
     * @param elements configuration elements, get in and pass out to maintain
     * settings
     *
     * Reconnects to twitch servers and sends connection back to
     * beginReadingMessages
     *
     */
    public void reconnect() {
        if (reconCount > 5) {
            LOGGER.severe("Reconnection failed after 5 attempts");
        } else {
            Thread botT;
            try {
                eventObL.addList("Restarting bot");
                socket = null;
                store.getBot().cancel();
                store.getBot().createBot();
                store.getBot().start(true);
                botT = new Thread() {
                    @Override
                    public void run() {
                        store.getBot().beginReadingMessages();
                    }
                };
                botT.start();

                eventObL.addList("Bot connected to chat");
            } catch (NullPointerException e) {
                CommonUtility.ERRORLOGGER.severe(e.toString());
                eventObL.addList("No instance to cancel, restart the application");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "A general error occurred in recon: {0}", e.toString());
                reconCount++;
            }
        }
    }

    /**
     * This method will start a sequence of events for starting the bot.
     *
     * 1) Load and Read the configuration file. 2) Connect to the Twitch API 3)
     * Start all periodic timers for broadcasting (if there are any) 4) Start a
     * blocking read on the socket for incoming message
     *
     * @param outstream
     * @param instream
     */
    public void createBot() {
        try {
            LOGGER.info("KfBot for Twitch " + BOT_VERSION + " by Raxa");
            socket = new Socket(store.getConfiguration().host, store.getConfiguration().port);
            socket.setKeepAlive(true);
            // test false disconnect
            //socket.setSoTimeout(5000);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("PASS " + store.getConfiguration().password);
            out.println("NICK " + store.getConfiguration().account);
            out.println("JOIN #" + store.getConfiguration().joinedChannel);
            //TODO alternate method of adding filters in bot's channel
            //out.println("JOIN #" + store.getConfiguration().account);
            out.println("CAP REQ :twitch.tv/tags");
            out.println("CAP REQ :twitch.tv/commands");
            //out.println("CAP REQ :twitch.tv/membership");
            cancelled = false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A general error occurred parsing the message: {0}", e.toString());
            e.printStackTrace();
            reconCount++;
        }
    }

    public void start(boolean reconnect) {
        try {

            //TODO make sure pubsub and sl auth lines up in final sent xml
            // Begin connecting to and listening to Twitch PubSub 
            //startPubSub(store, out);
            //Start StreamLabs listener
            if (!reconnect) {
                startSL(store, out);
            }

            // start checking for PING messages
            startPingPong(store, out);
            System.out.println("Recon? " + reconnect);
            if (!reconnect) {

                final String ReadyMessage = "/me > " + BOT_VERSION + " has joined the channel.";
                out.println("PRIVMSG #"
                        + store.getConfiguration().joinedChannel
                        + " :"
                        + ReadyMessage);
            }

            LOGGER.info("Bot is now ready for service.");

            //clear out old timers before
            // TimerManagement.getLHM().clear();
            // start all periodic timers for broadcasting events
            startTimers(store, out);

            // start doing a blocking read on the socket
            //beginReadingMessages();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error caught at bot start up: {0}");
            e.printStackTrace();
        }
    }

    /*
    ** This method checks for the Pings count every 10 minutes to ensure Socket is connected
    **
     */
    public void startPingPong(final Datastore store, final PrintStream out) {
        LOGGER.info("Starting to track pings");
        sPP = Executors.newSingleThreadScheduledExecutor();
        sPP.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                // System.out.println("DO SOME PING PONGS");
                int pCheck = pH.getPong();
                //System.out.println("Pong count: " + pCheck);
                if (pCheck == 0) {
                    LOGGER.severe("LOST CONNECTION ATTEMPTING TO RECONNECT");
                    store.getBot().reconnect();
                } else {
                    pH.resetPong();
                    out.println("PING");
                    //System.out.println("Sent Ping");
                }
            }
        }, 1, 4, TimeUnit.MINUTES);
    }


    /*
    * This method begins listening for PubSub info notices 
    *
    *
     */
    public void startPubSub(final Datastore store, final PrintStream out) {

        PubSubSubscriptionHandler.connect(store, out);

        //WhisperHandler.connect(store, out);
        PubSubBitsHandler.connect(store, out);
    }

    //start streamlabs listener
    public void startSL(final Datastore store, final PrintStream out) {
        DonationHandler slh = new DonationHandler(store, out);
        ThreadFactory tF = Executors.defaultThreadFactory();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor(tF);
        s.scheduleWithFixedDelay(slh, 0, 60, TimeUnit.SECONDS);
    }

    /*
    ** This will start the repeating commands based on what is found in XML
    **
     */
    public void startTimers(final Datastore store, final PrintStream out) {
        timers = new TimerManagement();
        timers.setupPeriodicBroadcast();
    }

}
