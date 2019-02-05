package com.twitchbotx.bot.client;

import com.twitchbotx.gui.controllers.DashboardController;
import java.io.PrintStream;

/**
 * This is a Twitch messenger for all outbound twitch messages.
 */
public final class TwitchMessenger {

    private final PrintStream outstream;

    private final String channel;

    public TwitchMessenger(final PrintStream stream, final String channel) {
        this.outstream = stream;
        this.channel = channel;
    }

    /**
     * This method will send a whisper out to a particular user. The user should
     * be include as part of the message.
     *
     * @param msg The message to be sent out to the channel.
     */
     public void sendWhisper(final String msg) {
        if (!msg.isEmpty()) {
            final String message = msg;
           DashboardController.wIRC.sendMessage(message, false);
        }
    }

    /**
     * This command will send a message out to a specific Twitch channel.
     *
     * It will also wrap the message in pretty text (> /me) before sending it
     * out.
     *
     * @param msg The message to be sent out to the channel
     */
    public void sendMessage(final String msg) {
         DashboardController.wIRC.sendMessage(msg, true);
        if (!msg.isEmpty()) {
            final String message = "/me > " + msg;
            DashboardController.wIRC.sendMessage(message, true);
        }
    }
    
     /**
     * This command will send a message out to the bot Twitch channel.
     *
     *
     * @param msg The message to be sent out to the channel
     */
   /* public void sendEditorMessage(final String msg) {
        String botChannel = guiHandler.bot.getStore().getConfiguration().account;
        if (!msg.isEmpty()) {
            final String message = "/me > " + msg;
            guiHandler.bot.getOut().println("PRIVMSG #"
                    + botChannel
                    + " "
                    + ":"
                    + message);
        }
    }*/
}
