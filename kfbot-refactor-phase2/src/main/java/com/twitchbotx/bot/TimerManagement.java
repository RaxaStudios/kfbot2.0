package com.twitchbotx.bot;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
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

    private final PrintStream outstream;
    private final ConfigParameters.Elements elements;
    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    /*
** This takes the information parsed in the start of the program under 
** elements "repeating" and "interval"
** "repeating" = True commands need to start when the bot starts
** all commands set to repeat need to start (including created/edited commands)
     */
    public TimerManagement(final ConfigParameters.Elements elements,
            final PrintStream stream) {
        this.elements = elements;
        this.outstream = stream;
    }

    public void setupPeriodicBroadcast(final ConfigParameters.Elements repeating) {
        for (int i = 0; i < elements.commandNodes.getLength(); i++) {
            Element ca = (Element) repeating.commandNodes.item(i);

            if (Boolean.parseBoolean(ca.getAttribute("repeating"))) {
                long d = Long.parseLong(ca.getAttribute("initialDelay")) * 1000L;
                Long l = Long.parseLong(ca.getAttribute("interval")) * 1000L;
                if (l < 60000L) {
                    System.out.println("Repeating interval too short for command " + ca.getAttribute("name"));
                } else {
                    new Timer().schedule(new rTimer(ca.getTextContent(), l), d);

                }
            }
        }
    }

    static class rTimer
            extends TimerTask implements Runnable {

        String message;
        long repeatingTimer;

        public rTimer(String msg, long timer) {
            this.message = msg;
            this.repeatingTimer = timer;
        }

        @Override
        public void run() {
            new Timer().schedule(new rTimer(this.message, this.repeatingTimer), this.repeatingTimer);
            /*sendTimer sendMessage = new sendTimer(this.message);*/
            //sendMessage.run();
            /*sendMessage(this.message);*/

            System.out.println("Starting repeating commands" + this.message);
        }
    }

    /**
     * This command will send a message out to a specific Twitch channel.
     *
     * It will also wrap the message in pretty text (> /me) before sending it
     * out.
     */
    static class sendTimer implements Runnable {

        private final PrintStream outstream;
        private final ConfigParameters.Elements elements;
        String msg1;

        public sendTimer(String message1) {
            this.msg1 = message1;
            this.outstream = null;
            this.elements = null;
        }

        private void sendMessage(final String msg) {
            final String message = "/me > " + msg;
            this.outstream.println("PRIVMSG #"
                    + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                    + " "
                    + ":"
                    + message);
        }

        public void run() {

        }
    }

    private void sendMessage(final String msg) {
        final String message = "/me > " + msg;
        this.outstream.println("PRIVMSG #"
                + this.elements.configNode.getElementsByTagName("myChannel").item(0).getTextContent()
                + " "
                + ":"
                + message);
    }
}
