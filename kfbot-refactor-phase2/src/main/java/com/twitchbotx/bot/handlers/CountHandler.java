package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;

import java.util.logging.Logger;

public final class CountHandler {

    private static final Logger LOGGER = Logger.getLogger(CountHandler.class.getSimpleName());

    private final Datastore store;

    String SQLURL;
    String USER;
    String PASS;
    static Connection con = null;
    static Statement stmt = null;
    static String sqlStatement = "";
    static boolean first = true;
    int minutes = 0;
    int hours = 12;

    private final TwitchMessenger messenger;

    public CountHandler(final Datastore store, final PrintStream out) {
        this.store = store;
        this.SQLURL = store.getConfiguration().sqlURL;
        this.USER = store.getConfiguration().sqlUser;
        this.PASS = store.getConfiguration().sqlPass;
        this.messenger = new TwitchMessenger(out, store.getConfiguration().joinedChannel);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    ** Allows for counters to be added, deleted, set, added to, and all totals calls
    **
    ** return name and value
     */
    public String addCounter(final String msg) {
        try {
            final String name = CommonUtility.getInputParameter("!cnt-add", msg, true);
            if (store.addCounter(name)) {
                return "Added counter [" + name + "]";
            }
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
        return "Syntax: !cnt-add [name]";
    }

    public String updateCount(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!countadd", msg, true);
            final int separator = parameters.indexOf(" ");
            final String name = parameters.substring(0, separator);
            final int delta = Integer.parseInt(parameters.substring(separator + 1));

            final boolean updated = store.updateCounter(name, delta);
            if (updated) {
                return "Counter [" + name + "] updated to " + delta;
            }

        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return "Syntax: !countadd [name] [value]";
    }

    public String deleteCounter(final String msg) {
        final String name = CommonUtility.getInputParameter("!cnt-delete", msg, true);
        final boolean deleted = store.deleteCounter(name);

        if (deleted) {
            return "Counter [" + name + "] deleted.";
        }
        return "Counter [" + name + "] not found.";
    }

    public String setCounter(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!cnt-set", msg, true);
            final int separator = parameters.indexOf(" ");
            final String name = parameters.substring(0, separator);
            final int value = Integer.parseInt(parameters.substring(separator + 1));

            boolean setted = store.setCounter(name, value);
            if (setted) {
                return "Counter [" + name + "] set to [" + Integer.toString(value) + "]";
            }

        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return "Syntax: !cnt-set [name] [value]";
    }

    public String getCurrentCount(final String msg) {
        final String name = CommonUtility.getInputParameter("!cnt-current", msg, true);
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Counter counter = store.getCounters().get(i);
            if (name.contentEquals(counter.name)) {
                return "Counter [" + name + "] is currently [" + counter.count + "]";
            }
        }
        return "Counter [" + name + "] not found.";
    }

    /**
     * This method will return the total number of counters and their respective
     * count.
     *
     * @return A string as the message to be sent out for all the counters.
     */
    public String totals() {
        String[][] counters = new String[store.getConfiguration().numCounters][2];
        int count = 0;
        for (int i = 0; i < store.getCounters().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            counters[i][0] = command.name;
            counters[i][1] = command.text;
            count++;
        }
        switch (count) {
            case 1:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1];
            case 2:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1];
            case 3:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1];
            case 4:
                return "Current totals: [" + counters[0][0] + "]: " + counters[0][1] + " [" + counters[1][0] + "]: " + counters[1][1] + " [" + counters[2][0] + "]: " + counters[2][1] + " " + counters[3][0] + ": " + counters[3][1];
            default:
                return "No counters available";
        }
    }
}
