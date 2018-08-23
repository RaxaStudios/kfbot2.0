package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.guiHandler;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Statement;

import java.util.logging.Logger;
import javafx.application.Platform;

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

    public static class SubHandler {

        /**
         * Deals with sub messages and routes a response to chat accordingly
         *
         * @param massGifted boolean
         * @param subGift boolean
         * @param subDisplayName display-name will be gifter or subscriber
         * @param giftRecip name of recipient of a sub gift
         * @param giftAmount number of gifted subs
         * @param subPoints tier 1 = 1, tier 2 = 2, tier 3 = 6 int
         * @param prime is this a prime sub?
         * @param subMonths String
         *
         * check for mass sub gift, single sub gift, resub, new sub form
         * response with appropriate names, length and tier
         *
         * variables: %user %months %recipient %gifts %tier
         * @return String to send to chat
         *
         */
        public static String handleSubMessage(
                boolean massGifted,
                boolean subGift,
                String subDisplayName,
                String giftRecip,
                int giftAmount,
                int subPoints,
                boolean prime,
                String subMonths) {
            String response = "";
            String tier = "";
            switch (subPoints) {
                case 1:
                    tier = "1";
                    break;
                case 2:
                    tier = "2";
                    break;
                case 6:
                    tier = "3";
                    break;
                default:
                    tier = "1";
                    break;
            }
            //first deal with normal + Prime subs
            if (!massGifted && !subGift) {
                if (Integer.parseInt(subMonths) < 2) {
                    //new subs
                    if (prime) {
                        // find a replace variable %user 
                        response = guiHandler.bot.getStore().getConfiguration().subNewPrimeReply;
                        response = response.replace("%user", subDisplayName).replace("%tier", tier);
                        return response;
                    } else {
                        // find a replace variable %user %tier
                        response = guiHandler.bot.getStore().getConfiguration().subNewNormalReply;
                        response = response.replace("%user", subDisplayName).replace("%tier", tier);
                        return response;
                    }
                } else {
                    if (prime) {
                        // find a replace variables %user %months
                        response = guiHandler.bot.getStore().getConfiguration().subPrimeReply;
                        response = response.replace("%user", subDisplayName).replace("%months", subMonths);
                    } else {
                        // find a replace variables %user %months %tier
                        response = guiHandler.bot.getStore().getConfiguration().subNormalReply;
                        response = response.replace("%user", subDisplayName).replace("%months", subMonths).replace("%tier", tier);
                    }
                }
            }
            // deal with single gifted sub
            if (!massGifted && subGift) {
                // find a replace variables %user %recipient %tier
                response = guiHandler.bot.getStore().getConfiguration().subSingleGiftReply;
                response = response.replace("%user", subDisplayName).replace("%recipient", giftRecip).replace("%tier", tier);
            }
            if (massGifted) {
                // find a replace variables %user %gifts %tier
                response = guiHandler.bot.getStore().getConfiguration().subMassGiftReply;
                response = response.replace("%user", subDisplayName).replace("%gifts", String.valueOf(giftAmount)).replace("%tier", tier);
            }

            // bundle and send reply to messenger and event list
            sendEvent(response);
            return response;
        }
        private static void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(event);
            }
        });
    }
    }
}
