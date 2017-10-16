package com.twitchbotx.bot;

import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.gui.guiHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the application for a Twitch Bot.
 */
public final class TwitchBotX {

    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());
    private static volatile boolean cancelled = false;
    private Datastore store;
    private PrintStream out;
    private BufferedReader in;
    private String dataIn;

    private static final String BOT_VERSION = "v2.00";

    private final ConfigParameters configuration = new ConfigParameters();

    /**
     * This method will begin reading for incoming messages from Twitch IRC API.
     *
     * @param store The database utility for accessing and updating.
     */
    public void beginReadingMessages(final Datastore store) {

        final CommandParser parser = new CommandParser(store, out);

        try {
            while (!cancelled) {
                dataIn = in.readLine();
                parser.parse(dataIn);
            }
            System.out.println("Bot shutting down");
            throw new NullPointerException();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An error occurred with I/O, perhaps with the Twitch API: {0}", e.toString());
            //start();
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, "Shutting down bot");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "A general error occurred parsing the message: {0}", e.toString());
        }
    }

    //method to stop reading loop
    public void cancel() {
        cancelled = true;
        try {
            guiHandler.socket.close();
            out.close();
            in.close();
        } catch (IOException ex) {
            LOGGER.severe("Unable to recreate Prinstream");
        }
    }

    /**
     * This method will start a sequence of events for starting the bot.
     *
     * 1) Load and Read the configuration file. 2) Connect to the Twitch API 3)
     * Start all periodic timers for broadcasting (if there are any) 4) Start a
     * blocking read on the socket for incoming message
     */
    public void createBot() {
        try {
            LOGGER.info("NecoBot for Twitch " + BOT_VERSION + " by Raxa");
            final Elements elements = configuration.parseConfiguration("./kfbot.xml");
            store = new XmlDatastore(elements);
            guiHandler.socket = new Socket(store.getConfiguration().host, store.getConfiguration().port);
            out = new PrintStream(guiHandler.socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(guiHandler.socket.getInputStream()));
            out.println("PASS " + store.getConfiguration().password);
            out.println("NICK " + store.getConfiguration().account);
            out.println("JOIN #" + store.getConfiguration().joinedChannel);
            out.println("CAP REQ :twitch.tv/tags");
            out.println("CAP REQ :twitch.tv/commands");
            cancelled = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            final String ReadyMessage = "/me > " + BOT_VERSION + " has joined the channel.";
            out.println("PRIVMSG #"
                    + store.getConfiguration().joinedChannel
                    + " :"
                    + ReadyMessage);

            LOGGER.info("Bot is now ready for service.");
            // start doing a blocking read on the socket
            beginReadingMessages(store);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error caught at bot start up: {0}", e.toString());
        }
    }
}
