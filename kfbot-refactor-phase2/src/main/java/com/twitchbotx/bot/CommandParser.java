package com.twitchbotx.bot;

import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.bot.handlers.*;

import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.application.Platform;

/**
 * This class is used to parse all commands flowing through it.
 */
public final class CommandParser {

    private static final Logger LOGGER = Logger.getLogger(CommandParser.class.getSimpleName());

    // For handling all normal commands
    private final CommandOptionHandler commandOptionsHandler;

    // A stream for communicating to twitch chat through IRC
    private final PrintStream outstream;

    // For handling all youtube link messaging
    private final YoutubeHandler youtubeHandler;

    // For moderation filtering options
    private final ModerationHandler moderationHandler;

    // For pyramid detection
    private final PyramidDetector pyramidDetector;

    // For Twitch statuses
    private final TwitchStatusHandler twitchStatusHandler;

    // For counter handling
    private final CountHandler countHandler;

    // For marathon system
    private final MarathonHandler mHandler;

    // For lottery system
    public static LotteryHandler.Lotto lotto = new LotteryHandler.Lotto();
    public static LotteryHandler.SongList songs = new LotteryHandler.SongList();
    final ConfigParameters configuration = new ConfigParameters();

    // For handling SQL transactions
    private final sqlHandler sql;

    // For handling displayname capitalization 
    public static String displayName = "";

    // For filter handling
    private final FilterHandler filterHandler;

    // For sending message out
    private final TwitchMessenger messenger;

    // Variable for ping testing system
    private static int p = 0;

    // Store for lottery handler use
    private final Datastore store;

    // A simple constructor for this class that takes in the XML elements
    // for quick modification
    public CommandParser(final Datastore store, final PrintStream stream) {

        //TODO match xml file with all commands to ensure proper authorization and directions
        
        // all the handlers for different messages
        this.commandOptionsHandler = new CommandOptionHandler(store);
        this.pyramidDetector = new PyramidDetector(store);
        this.twitchStatusHandler = new TwitchStatusHandler(store);
        this.countHandler = new CountHandler(store, stream);
        this.filterHandler = new FilterHandler(store);
        this.youtubeHandler = new YoutubeHandler(store, stream);
        this.moderationHandler = new ModerationHandler(store, stream);
        this.messenger = new TwitchMessenger(stream, store.getConfiguration().joinedChannel);
        this.sql = new sqlHandler(store, stream);
        this.outstream = stream;
        this.store = store;
        this.mHandler = new MarathonHandler(store, stream);
    }

    public void addPing() {
        TimerManagement.pongHandler pong = TwitchBotX.pH;
        pong.addPong();
        System.out.println("Adding a ping from CommandParser");
    }

    /**
     * This method will start handling all the commands and delegating it to the
     * proper handlers. Uses XML file to determine requirements for commands.
     * Requirements set by !command-auth Command enabled/disabled by
     * !command-enable
     *
     * @param mod A boolean field which indicates whether this is a mod message.
     *
     * @param sub A boolean field which indicates whether this is a subscriber
     * message.
     *
     * @param trailing The trailing message that accompany the command
     */
    private void handleCommand(final String username, final boolean mod, final boolean sub, String trailing) {
        if (trailing.contains("")) {
            trailing = trailing.replaceAll("", "");
            trailing = trailing.replaceFirst("ACTION ", "");
        }
        if (trailing.startsWith("!")) {
            String cmd;
            int cmdEnd = trailing.indexOf(" ");
            if (cmdEnd == -1) {
                trailing = trailing.toLowerCase();
                System.out.println("TRAIL: " + trailing);
            } else {
                cmd = trailing.substring(trailing.indexOf("!"), trailing.indexOf(" "));
                System.out.println(cmd + " COMMAND");
            }
        }

        //
//        final boolean detected = pyramidDetector.pyramidDetection(username, trailing);
//        if(detected) {
//            twitchMessenger.sendMessage(store.getConfiguration().pyramidResponse);
//        }
        youtubeHandler.handleLinkRequest(trailing);
        moderationHandler.handleTool(username, trailing);
        //add check for lottery entrants
        if (lotto.getLottoStatus()) {
            String keyword = lotto.getLottoName();
            if (keyword == null) {
                keyword = "";
            }
            if (trailing.startsWith(keyword) && !keyword.equals("")) {
                lotto.addUser(username, sub);
            }
        }

        if (!trailing.startsWith("!")) {
            return;
        }

        if (trailing.startsWith("!test1")) {
            sendEvent("Test event command");
        }

        if (trailing.startsWith("!uptime")) {
            LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[]{username, mod, sub});
            if (commandOptionsHandler.checkAuthorization("!uptime", username, mod, sub)) {
                messenger.sendMessage(twitchStatusHandler.uptime(trailing));
            }
            return;
        }

        if (trailing.startsWith("!followage")) {
            if (commandOptionsHandler.checkAuthorization("!followage", username, mod, sub)) {
                String user = username.toLowerCase();
                messenger.sendMessage(twitchStatusHandler.followage(user));
            }
            return;
        }

        //being marathon system
        if (trailing.startsWith("!addPoints")) {
            if (commandOptionsHandler.checkAuthorization("!addPoints", username, mod, sub)) {
                mHandler.addPoints(trailing);

            }
        }
        if (trailing.startsWith("!setTime")) {
            if (commandOptionsHandler.checkAuthorization("!setTime", username, mod, sub)) {
                mHandler.setTime(trailing);
            }
        }

        if (trailing.startsWith("!setBaseTime")) {
            if (commandOptionsHandler.checkAuthorization("!setBaseTime", username, mod, sub)) {
                mHandler.setBaseTime(trailing);
            }
        }

        if (trailing.startsWith("!setMinValue")) {
            if (commandOptionsHandler.checkAuthorization("!setMinValue", username, mod, sub)) {
                mHandler.setMinValue(trailing);
            }
        }

        if (trailing.startsWith("!start")) {
            if (commandOptionsHandler.checkAuthorization("!start", username, mod, sub)) {
                mHandler.startTimer();
            }
        }
        //TODO add in donation war system
        /* if (trailing.startsWith("!addSubPoints")) {
            if (commandHandler.checkAuthorization("!setTime", username, mod, sub)) {
                countHandler.addSubPointTracker(trailing);
            }
        }
        if (trailing.startsWith("!dwpoints")) {
            if (commandHandler.checkAuthorization("!dwpoints", username, mod, sub)) {
                countHandler.getDonationPoints();
            }
        }

        if (trailing.startsWith("!addDonationPoints")) {
            if (commandHandler.checkAuthorization("!addDonationPoints", username, mod, sub)) {
                countHandler.addDonation(trailing);
            }
        }

        if (trailing.startsWith("!setDonationPoints")) {
            if (commandHandler.checkAuthorization("!setDonationPoints", username, mod, sub)) {
                countHandler.setDonationPoints(trailing);
            }
        }
        if (trailing.startsWith("!donation-add")) {
            if (commandHandler.checkAuthorization("!donation-add", username, mod, sub)) {
                countHandler.donateAdd(trailing);
            }
        }

        if (trailing.startsWith("!donation-delete")) {
            if (commandHandler.checkAuthorization("!donation-delete", username, mod, sub)) {
                countHandler.donateDel(trailing);
            }
        }

        if (trailing.startsWith("!totalTime")) {
            if (commandHandler.checkAuthorization("!totalTime", username, mod, sub)) {
                countHandler.totalTime();
            }
        }

        if (trailing.startsWith("!highlight")) {
            if (commandHandler.checkAuthorization("!highlight", username, mod, sub)) {
                commandHandler.highlight();
            }
            return;
        }
         */

        //begin raffle system commands
        if (trailing.startsWith("!lottery-open")) {
            if (commandOptionsHandler.checkAuthorization("!lottery-open", username, mod, sub)) {
                lotto.lottoOpen(trailing);
            }
            return;
        }

        if (trailing.startsWith("!lottery-clear")) {
            if (commandOptionsHandler.checkAuthorization("!lottery-clear", username, mod, sub)) {
                lotto.lottoClear();
            }
            return;
        }

        if (trailing.startsWith("!unlottery")) {
            lotto.leaveLotto(username);
            return;
        }

        if (trailing.startsWith("!draw")) {
            if (commandOptionsHandler.checkAuthorization("!draw", username, mod, sub)) {
                lotto.drawLotto();
            }
            return;
        }

        if (trailing.startsWith("!song-open")) {
            if (commandOptionsHandler.checkAuthorization("!song-open", username, mod, sub)) {
                songs.songOpen();
            }
            return;
        }

        if (trailing.startsWith("!song-close")) {
            if (commandOptionsHandler.checkAuthorization("!song-close", username, mod, sub)) {
                songs.songClose();
            }
            return;
        }

        if (trailing.startsWith("!song-reset")) {
            if (commandOptionsHandler.checkAuthorization("!song-reset", username, mod, sub)) {
                songs.songReset();
            }
            return;
        }

        if (trailing.startsWith("!song-draw")) {
            if (commandOptionsHandler.checkAuthorization("!song-draw", username, mod, sub)) {
                songs.drawSong();
            }
            return;
        }

        if (trailing.startsWith("!song")) {
            if (songs.getSongStatus()) {
                songs.addUser(username, trailing);
            }
            return;
        }

        if (trailing.startsWith("!unsong")) {
            songs.leaveSong(username);
            return;
        }

        // begin sql system
        if (trailing.startsWith("!s-game-add")) {
            if (commandOptionsHandler.checkAuthorization("!s-game-add", username, mod, sub)) {
                //add a new game to sql table, default to 0 points
                sql.addGame(trailing);
            }
            return;
        }
        if (trailing.startsWith("!s-game-delete")) {
            if (commandOptionsHandler.checkAuthorization("!s-game-delete", username, mod, sub)) {
                //delete game from sql table, including entire entry(name + point value)
                sql.deleteGame(trailing);
            }
            return;
        }

        if (trailing.startsWith("!s-set-name")) {
            if (commandOptionsHandler.checkAuthorization("!s-set-name", username, mod, sub)) {
                //manual overwrite of game name
                sql.setName(trailing);
            }
            return;
        }

        if (trailing.startsWith("!s-set-points")) {
            if (commandOptionsHandler.checkAuthorization("!s-set-points", username, mod, sub)) {
                //manual overwrite of point value
                sql.setPoints(trailing);
            }
            return;
        }

        if (trailing.startsWith("!s-addPoints") || trailing.startsWith("!s-addpoints")) {
            if (commandOptionsHandler.checkAuthorization("!s-addPoints", username, mod, sub)) {
                sql.addPoints(trailing);
            }
            return;
        }

        if (trailing.startsWith("!points")) {
            if (commandOptionsHandler.checkAuthorization("!points", username, mod, sub)) {
                //if trailing empty put all names + point amount
                //else put game points ie !points Game1
                sql.getPoints(trailing, username);
            }
            return;
        }

        if (trailing.startsWith("!s-status")) {
            if (commandOptionsHandler.checkAuthorization("!s-addPoints", username, mod, sub)) {
                sql.sStatus(trailing);
            }
            return;
        }

//        if (trailing.startsWith("!commands")) {
//            if (commandOptionsHandler.checkAuthorization("!commands", username, mod, sub)) {
//                commandOptionsHandler.commands(username, mod, sub);
//            }
//        }
        if (trailing.startsWith("!command-add")) {
            if (commandOptionsHandler.checkAuthorization("!command-add", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.addCommand(trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-delete")) {
            if (commandOptionsHandler.checkAuthorization("!command-delete", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.deleteCommand(trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-edit")) {
            if (commandOptionsHandler.checkAuthorization("!command-edit", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.editCommand(trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-auth")) {
            if (commandOptionsHandler.checkAuthorization("!command-auth", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.authorizeCommand(username, trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-enable")) {
            if (commandOptionsHandler.checkAuthorization("!command-enable", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.commandEnable(trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-disable")) {
            if (commandOptionsHandler.checkAuthorization("!command-disable", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.commandDisable(trailing));
            }
            return;
        }
        if (trailing.startsWith("!command-sound")) {
            if (commandOptionsHandler.checkAuthorization("!command-sound", username, mod, sub)) {
                messenger.sendMessage(commandOptionsHandler.commandSound(trailing));
            }
            return;
        }

        if (trailing.startsWith("!filter-all")) {
            if (commandOptionsHandler.checkAuthorization("!filter-all", username, mod, sub)) {
                messenger.sendMessage(filterHandler.getAllFilters(trailing, username));
            }
        }
        if (trailing.startsWith("!filter-add")) {
            if (commandOptionsHandler.checkAuthorization("!filter-add", username, mod, sub)) {
                messenger.sendMessage(filterHandler.addFilter(trailing, username));
            }
        }
        if (trailing.startsWith("!filter-delete")) {
            if (commandOptionsHandler.checkAuthorization("!filter-delete", username, mod, sub)) {
                messenger.sendMessage(filterHandler.deleteFilter(trailing, username));
            }
        }

        if (trailing.startsWith("!set-msgCache")) {
            if (commandOptionsHandler.checkAuthorization("!set-msgCache", username, mod, sub)) {
                messenger.sendMessage(pyramidDetector.setMessageCacheSize(trailing));
            }
            return;
        }
        if (trailing.startsWith("!set-pyramidResponse")) {
            if (commandOptionsHandler.checkAuthorization("!set-pyramidResponse", username, mod, sub)) {
                messenger.sendMessage(pyramidDetector.setPyramidResponse(trailing));
            }
            return;
        }
        if (trailing.startsWith("!cnt-add")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-add", username, mod, sub)) {
                messenger.sendMessage(countHandler.addCounter(trailing));
            }
            return;
        }
        if (trailing.startsWith("!cnt-delete")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-delete", username, mod, sub)) {
                messenger.sendMessage(countHandler.deleteCounter(trailing));
            }
            return;
        }
        if (trailing.startsWith("!cnt-set")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-set", username, mod, sub)) {
                messenger.sendMessage(countHandler.setCounter(trailing));
            }
            return;
        }
        if (trailing.startsWith("!cnt-current")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-current", username, mod, sub)) {
                messenger.sendMessage(countHandler.getCurrentCount(trailing));
            }
            return;
        }
        if (trailing.startsWith("!countadd")) {
            if (commandOptionsHandler.checkAuthorization("!countadd", username, mod, sub)) {
                messenger.sendMessage(countHandler.updateCount(trailing));
            }
            return;
        }

        if (trailing.startsWith("!totals")) {
            if (commandOptionsHandler.checkAuthorization("!totals", username, mod, sub)) {
                messenger.sendMessage(countHandler.totals());
            }
            return;
        }
        messenger.sendMessage(commandOptionsHandler.parseForUserCommands(trailing, username, mod, sub));
    }

    /**
     * This method parses all incoming messages from Twitch IRC in the bots
     * channel.
     *
     * @param user Username of person using the command.
     * @param msg A string that represents the message type.
     * @param channel Channel that the message is coming from.
     */
    public void handleEditorCommand(String user, String msg, String channel) {
        // Check for editor level of user
        int level = 0;
        if (store.getConfiguration().joinedChannel.equalsIgnoreCase(user)) {
            level = 600;
        } else {
            for (int i = 0; i < store.getEditors().size(); i++) {
                final ConfigParameters.Editor editor = store.getEditors().get(i);
                if (editor.username.equalsIgnoreCase(user)) {
                    level = editor.level;
                }
            }
        }
        messenger.sendEditorMessage(editorCommandHandler.parseForCommand(user, level, msg, channel));
    }

    /**
     * This method parses all incoming messages from Twitch IRC.
     *
     * @param msg A string that represents the message type.
     */
    public void parse(String msg) {
        try {
            // If nothing is provided, exit out of here
            if (msg == null || msg.isEmpty()) {
                return;
            }

            // A ping was sent by the twitch server, complete the handshake
            // by sending it back pong with the message.
            if (msg.startsWith("PING")) {
                final int trailingStart = msg.indexOf(" :");
                final String trailing = msg.substring(trailingStart + 2);
                this.outstream.println("PONG :" + trailing);
                return;
            }
            if (msg.startsWith("PONG")) {
                addPing();
            }

            boolean isMod = false;
            boolean isSub = false;
            String username = "";
            String chanFind = msg;

            // This is a message from a user.
            // If it's the broadcaster, he/she is a mod.
            LOGGER.info(msg);
            if (msg.startsWith("@badges=broadcaster/1")) {
                isMod = true;
            }

            // Find the mod indication
            final int modPosition = msg.indexOf("mod=") + 4;
            if ("1".equals(msg.substring(modPosition, modPosition + 1))) {
                isMod = true;
            }

            // Find the subscriber indication
            final int subPosition = msg.indexOf("subscriber=") + 11;
            if ("1".equals(msg.substring(subPosition, subPosition + 1))) {
                isSub = true;
            }

            // Find the username
            // User-id search for V5 switch
            /*if (msg.contains("user-id=")){
                int usernameStart = msg.indexOf("user-id=", msg.indexOf(";"));
                System.out.println(usernameStart);
            username = msg.substring(msg.indexOf("user-id=") + 8, msg.indexOf(";", msg.indexOf("user-id=")));
            System.out.println(username + " USERNAME");
            }*/
            if (msg.contains("user-type=")) {
                int usernameStart = msg.indexOf(":", msg.indexOf("user-type="));
                int usernameEnd = msg.indexOf("!", usernameStart);
                if (usernameStart != -1 && usernameEnd != -1) {
                    username = msg.substring(usernameStart + 1, usernameEnd).toLowerCase();
                    System.out.println(username + " USERNAME");
                }
            }

            // Split the message into pieces to find the real message
            final int msgPosition = msg.indexOf("user-type=");

            // No message to be processed
            if (msgPosition == -1) {
                return;
            }
            msg = msg.substring(msgPosition);

            // Find the # for the channel, so we can figured out what type
            // of message this is.
            final int channelPosition = msg.indexOf("#");
            if (msgPosition == -1) {
                return;
            }

            // Ensure we can find "PRIVMSG" as an indication that this is a
            // user message, make sure we only search a limited bound, because
            // somebody can potentially fake a mod by including "PRIVMSG" 
            // in their message
            final String hasPrivMsg = msg.substring(0, channelPosition);
            final int privMsgIndex = hasPrivMsg.indexOf("PRIVMSG");
            if (privMsgIndex == -1) {
                return;
            }

            // Capture the raw message, and find the message used
            final int msgIndex = msg.indexOf(":", channelPosition);

            // No message found, return immediately
            if (msgIndex == -1) {
                return;
            }

            msg = msg.substring(msgIndex + 1);

            // Determine where message is from
            // filter system access via bot channel's chat
            final int channelName = chanFind.indexOf("#", chanFind.indexOf("PRIVMSG"));
            final int chanIndex = chanFind.indexOf(" ", channelName);
            String channel = chanFind.substring(channelName + 1, chanIndex);
            String botName = store.getConfiguration().account;
            System.out.println(channel + " " + botName);
            if (channel.equalsIgnoreCase(botName)) {
                handleEditorCommand(username, msg, channel);
            } else {

                // Handle the message
                handleCommand(username, isMod, isSub, msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Error detected in parsing a message: throwing away message ", e.toString());
        }
    }

    //Method to add events to the GUI event list
    //Stored in the store, created in DashboardController
    //to address thread safety and concurrency
    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                store.getEventList().addList(event);
            }
        });
    }
}
