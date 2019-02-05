package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.guiHandler;
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

    public CountHandler(final Datastore store) {
        this.store = store;
        this.SQLURL = store.getConfiguration().sqlURL;
        this.USER = store.getConfiguration().sqlUser;
        this.PASS = store.getConfiguration().sqlPass;
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

        // TODO: Place in appropriate higher level
        public enum SubType{MASSGIFT, SINGLEGIFT, NEWPRIMESUB, NEWSUB, PRIMERESUB, RESUB}

        // TODO: The returned value should be one of the parameters passed in to handleSubMessage
        private static SubType assessSubType(boolean massGift, boolean singleGift, String subMonths, boolean prime) {
            if (massGift) {
                return SubType.MASSGIFT;
            }

            if (singleGift) {
                return SubType.SINGLEGIFT;
            }

            if (Integer.parseInt(subMonths) < 2 && prime) {
                return SubType.NEWPRIMESUB;
            }

            if (Integer.parseInt(subMonths) < 2) {
                return SubType.NEWSUB;
            }

            if (prime) {
                return SubType.PRIMERESUB;
            }

            return SubType.RESUB;
        }

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
            boolean massGift,
            boolean singleGift,
            String subDisplayName,
            String giftRecipient,
            int giftAmount,
            int subPoints,
            boolean prime,
            String subMonths)
        {
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

            String response = "";
            SubType subType = assessSubType(massGift, singleGift, subMonths, prime);

            // deal with mass gift sub
            switch (subType) {
                case MASSGIFT:
                    response = massGiftMessage(subDisplayName, giftAmount, tier);
                    break;
                case SINGLEGIFT:
                    response = singleGiftMessage(subDisplayName, giftRecipient, tier);
                    break;
                case NEWPRIMESUB:
                    response = newPrimeSubMessage(subDisplayName, tier);
                    break;
                case NEWSUB:
                    response = newSubMessage(subDisplayName, tier);
                    break;
                case PRIMERESUB:
                    response = primeResubMessage(subDisplayName, subMonths)
                    break;
                case RESUB:
                    response = resubMessage(subDisplayName, subMonths, tier)
                    break;
                default:
                    // TODO: Log error because something has gone horribly wrong
                    response = "";
                    break;
            }

            // bundle and send reply to messenger and event list
            sendEvent(response);
            return response;
        }

        private static String massGiftMessage(String subDisplayName, int giftAmount, String tier) {
            String template = guiHandler.bot.getStore().getConfiguration().subMassGiftReply;
            return template.replace("%user", subDisplayName).replace("%gifts", String.valueOf(giftAmount)).replace("%tier", tier);
        }

        private static String singleGiftMessage(String subDisplayName, String giftRecipient, String tier) {
            String template = guiHandler.bot.getStore().getConfiguration().subSingleGiftReply;
            return template.replace("%user", subDisplayName).replace("%recipient", giftRecipient).replace("%tier", tier);
        }

        private static String newPrimeSubMessage(String subDisplayName, String tier) {
            String template = guiHandler.bot.getStore().getConfiguration().subNewPrimeReply;
            return template.replace("%user", subDisplayName).replace("%tier", tier);
        }

        private static String newSubMessage(String subDisplayName, String tier) {
            String template = guiHandler.bot.getStore().getConfiguration().subNewNormalReply;
            return template.replace("%user", subDisplayName).replace("%tier", tier);
        }

        private static String primeResubMessage(String subDisplayName, String subMonths) {
            String template = guiHandler.bot.getStore().getConfiguration().subPrimeReply;
            return template.replace("%user", subDisplayName).replace("%months", subMonths);
        }

        private static String resubMessage(String subDisplayName, String subMonths) {
            String template = guiHandler.bot.getStore().getConfiguration().subNormalReply;
            return template.replace("%user", subDisplayName).replace("%months", subMonths).replace("%tier", tier);
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
