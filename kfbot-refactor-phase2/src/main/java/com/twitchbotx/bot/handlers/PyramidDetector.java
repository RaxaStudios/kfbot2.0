package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PyramidDetector {

    private static final Logger LOGGER = Logger.getLogger(PyramidDetector.class.getSimpleName());

    private final List<CachedMessage> recentMessages = new ArrayList<>();

    private final Datastore store;

    public PyramidDetector(final Datastore store) {
        this.store = store;
    }

    /**
     * A simple inner class for storing cached messages.
     *
     * It simply stores the username and the message to prevent pyramids.
     */
    public static class CachedMessage {
        public String user;
        public String msg;

        public CachedMessage(final String username, final String message) {
            this.user = username;
            this.msg = message;
            System.out.println(this.user + this.msg + " CACHED MESSAGES");
        }

        public String getUser() {
            return user;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "CachedMessage{" + "user=" + user + ", msg=" + msg + '}';
        }
    }

    public String setMessageCacheSize(String msg) {
        try {
            String value = CommonUtility.getInputParameter("!set-msgCache", msg, true);

            int c = Integer.parseInt(value);
            if ((c < 2) || (c > 100)) {
                throw new IllegalArgumentException();
            }

            store.modifyConfiguration("recentMessageCacheSize", value);
            return "Cache size set to [" + value + "] messages for pyramid detection.";
        } catch (IllegalArgumentException e) {
            return "Syntax: !set-msgcache [2-100]";
        }
    }

    public String setPyramidResponse(String msg) {
        try {
            String value = CommonUtility.getInputParameter("!set-pyramidResponse", msg, false);
            store.modifyConfiguration("pyramidResponse", value);

            return "Pyramid response set to [" + value + "]";
        } catch (IllegalArgumentException e) {
            return "Syntax: !set-pyramidResponse [msg]";
        }
    }

    /**
     * This method is a quick and dirty solution for pyramid detection.
     *
     * It basically caches the last 15 messages, and user mapping, and does a
     * check on whether or not the user is doing a pattern of 3.
     *
     * @param user A given user that say a given message
     *
     * @param msg A message provided by the user
     */
    public boolean pyramidDetection(final String user, String msg) {
        recentMessages.add(new CachedMessage(user, msg));
        if (recentMessages.size() > store.getConfiguration().recentMessageCacheSize) {
            recentMessages.remove(0);
        }
        int patternEnd = msg.indexOf(" ");
        String pattern;
        if (patternEnd == -1) {
            pattern = msg;
            System.out.println(pattern + " PATTERN1");
        } else {
            pattern = msg.substring(0, msg.indexOf(" "));
            System.out.println(pattern + " PATTERN2");
        }
        if (!msg.contentEquals(pattern + " " + pattern + " " + pattern)) {
            System.out.println(msg + " IF MSG DOES NOT TEST");
            return false;
        }
        int patternCount = 3;
        for (int i = recentMessages.size() - 2; i >= 0; i--) {
            CachedMessage cm = recentMessages.get(i);
            if ((patternCount == 3) && (cm.getMsg().contentEquals(pattern + " " + pattern)) && (cm.getUser().contentEquals(user))) {
                System.out.println(cm.getMsg() + " CACHED MESSAGE PATTERN 2");
                patternCount = 2;
            } else if ((patternCount == 2) && (cm.getMsg().contentEquals(pattern)) && (cm.getUser().contentEquals(user))) {
                return true;
            }
        }

        return false;
    }
}
