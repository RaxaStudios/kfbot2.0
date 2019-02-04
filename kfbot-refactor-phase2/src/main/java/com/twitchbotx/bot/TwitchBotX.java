package com.twitchbotx.bot;

//import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.bot.TimerManagement.Task;
import com.twitchbotx.bot.handlers.CommonUtility;
import com.twitchbotx.bot.handlers.DonationHandler;
import com.twitchbotx.bot.handlers.PubSubBitsHandler;
import com.twitchbotx.bot.handlers.PubSubSubscriptionHandler;
import com.twitchbotx.bot.handlers.WhisperHandler;
import com.twitchbotx.gui.controllers.DashboardController;
import static com.twitchbotx.gui.controllers.DashboardController.eventObL;
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
    private String dataIn;
    private int reconCount = 0;
    private DashboardController dc;
    private ScheduledExecutorService sPP;
    private ScheduledExecutorService timedManagement;
    public TimerManagement timers;
    public static TimerManagement.pongHandler pH = new TimerManagement.pongHandler();
    private final ConfigParameters configuration = new ConfigParameters();
    public boolean rogue;

    public TwitchBotX(Datastore getStore) {
        this.dc = new DashboardController();
        this.store = getStore;
        this.rogue = false;
        LOGGER = CommonUtility.ERRORLOGGER;
    }

    public Datastore getStore() {
        return this.store;
    }

    public void start(boolean reconnect) {
        try {

            //Start StreamLabs listener
            //Send ready message on new connection
            System.out.println("Recon? " + reconnect);
            if (!reconnect) {
                startSL(store);
            }

            LOGGER.info("Bot is now ready for service.");

            //clear out old timers before
            // TimerManagement.getLHM().clear();
            // start all periodic timers for broadcasting events
            TimerManagement.LHM.clear();
            startTimers(store);

            // start doing a blocking read on the socket
            //beginReadingMessages();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error caught at bot start up: {0}");
            e.printStackTrace();
        }
    }

    //start streamlabs listener
    public void startSL(final Datastore store) {
        DonationHandler slh = new DonationHandler(store);
        ThreadFactory tF = Executors.defaultThreadFactory();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor(tF);
        s.scheduleWithFixedDelay(slh, 0, 60, TimeUnit.SECONDS);
    }

    /*
    ** This will start the repeating commands based on what is found in XML
    **
     */
    public void startTimers(final Datastore store) {
        timers = new TimerManagement();
        timers.setupPeriodicBroadcast();
    }

}
