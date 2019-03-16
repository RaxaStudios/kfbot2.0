package com.twitchbotx.bot;

import com.twitchbotx.bot.handlers.*;
import com.twitchbotx.gui.controllers.ConfigurationController;
import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.application.Platform;

/**
 * This class is used to parse all commands flowing through it.
 */
public class CommandParser {

    PrintStream s;

    private static final Logger LOGGER = Logger.getLogger(CommandParser.class.getSimpleName());

    // For handling all normal commands
    private final CommandOptionHandler commandOptionsHandler;

    // For handling all youtube link messaging
    private final YoutubeHandler youtubeHandler;

    // For moderation filtering options
    public static ModerationHandler moderationHandler;

    // For pyramid detection
    private final PyramidDetector pyramidDetector;

    // For Twitch statuses
    public static TwitchStatusHandler twitchStatusHandler;

    // For counter handling
    private final CountHandler countHandler;

    // For marathon system
    private final MarathonHandler mHandler;

    // For event systems(bits/raids/subs)
    private final EventHandler eHandler;

    // For lottery system
    public static LotteryHandler.Lotto lotto = new LotteryHandler.Lotto();
    public static LotteryHandler.SongList songs = new LotteryHandler.SongList();
    final ConfigParameters configuration = new ConfigParameters();
    public static PollHandler pHandler = new PollHandler();

    // For handling SQL transactions
    private final sqlHandler sql;
    public static SpoopathonHandler spoop = new SpoopathonHandler();

    // For handling displayname capitalization 
    public static String displayName = "";

    // For filter handling
    public static FilterHandler filterHandler;

    // Store for lottery handler use
    private final Datastore store;

    // A simple constructor for this class that takes in the XML elements
    // for quick modification
    public CommandParser(final Datastore store) {

        //TODO match xml file with all commands to ensure proper authorization and directions
        // all the handlers for different messages
        this.commandOptionsHandler = new CommandOptionHandler(store);
        this.pyramidDetector = new PyramidDetector(store);
        twitchStatusHandler = new TwitchStatusHandler();
        this.countHandler = new CountHandler(store);
        filterHandler = new FilterHandler(store);
        moderationHandler = new ModerationHandler(store);
        filterHandler.addListener(moderationHandler);
        this.youtubeHandler = new YoutubeHandler(store);
        this.sql = new sqlHandler(store);
        this.store = store;
        this.mHandler = new MarathonHandler(store);
        this.eHandler = new EventHandler(store);
        try {
            s = new PrintStream(new FileOutputStream("errors.log", true), true);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMessage(final String msg, boolean action) {

        DashboardController.wIRC.sendMessage(msg, action);
    }

    private void sendEditorMessage(String msg) {
        DashboardController.wIRC.sendEditorMessage(msg);
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
    private void handleCommand(String username, final boolean mod, final boolean sub, String trailing, String msgId) {
        if (trailing.contains("")) {
            trailing = trailing.replaceAll("", "");
            trailing = trailing.replaceFirst("ACTION ", "");
        }
        if (trailing.startsWith("!")) {
            String cmd;
            int cmdEnd = trailing.indexOf(" ");
            if (cmdEnd == -1) {
                trailing = trailing.toLowerCase();

            } else {
                cmd = trailing.substring(trailing.indexOf("!"), trailing.indexOf(" "));
                System.out.println(cmd + " COMMAND");
            }
        }

// comment out until needed/wanted back in
//        final boolean detected = pyramidDetector.pyramidDetection(username, trailing);
//        if(detected) {
//            twitchMessenger.sendMessage(store.getConfiguration().pyramidResponse);
//        }
        youtubeHandler.handleLinkRequest(trailing);
        moderationHandler.handleTool(username, trailing, msgId); // TODO implement msg-id grabbing and figure out delete flag
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
        if (pHandler.getRunning()) {
            if (pHandler.containsKey(trailing)) {
                pHandler.addVote(trailing, username);
            } else if (Character.isDigit(trailing.charAt(0))) {
                pHandler.addNumericVote(trailing, username);
            }
        }

        if (!trailing.startsWith("!") && (username.equalsIgnoreCase("Raxa") || username.equalsIgnoreCase("kungfufruitcup"))) {
            return;
        }

        // test purpose commands
        if (trailing.startsWith("!recon") && (username.equalsIgnoreCase("Raxa") || username.equalsIgnoreCase("kungfufruitcup"))) {
            parse(":tmi.twitch.tv RECONNECT");
        }

        if (trailing.startsWith("!rogue") && (username.equalsIgnoreCase("Raxa"))) {
            guiHandler.bot.rogue = true;
            ConfigurationController.goRogue.begin();
        }
        if (trailing.startsWith("!behave") && (username.equalsIgnoreCase("Raxa"))) {
            guiHandler.bot.rogue = false;
            ConfigurationController.goRogue.end();
        }

        if (trailing.startsWith("!test1") && (username.equalsIgnoreCase("Raxa"))) {
            sendEvent("Test event command");
        }

        if (trailing.startsWith("!whisper") && (username.equalsIgnoreCase("Raxa"))) {
            sendMessage("/w raxa test whisper message", false);
        }

        if (trailing.equalsIgnoreCase("!error") && (username.equalsIgnoreCase("Raxa"))) {
            CommonUtility.writeError("This is a test error sent from chat");
            return;
        }
        if (trailing.equalsIgnoreCase("!error2") && (username.equalsIgnoreCase("Raxa"))) {
            try {
                throw new IllegalArgumentException("Error testing illegally");
            } catch (Exception e) {
                try {
                    e.printStackTrace(s);
                } catch (Exception pe) {
                    pe.printStackTrace();
                }
                CommonUtility.writeError(e.toString());
            }
            return;
        }
        if (trailing.equalsIgnoreCase("!error3") && (username.equalsIgnoreCase("Raxa"))) {
            try {
                throw new IllegalArgumentException("Secondary illegal test");
            } catch (Exception e) {
                e.printStackTrace(s);
            }
            return;
        }

        if (trailing.startsWith("!uptime")) {
            if (commandOptionsHandler.checkAuthorization("!uptime", username, mod, sub)) {
                sendMessage(twitchStatusHandler.uptime(trailing), true);
            }
            return;
        }

        if (trailing.startsWith("!followage")) {
            if (commandOptionsHandler.checkAuthorization("!followage", username, mod, sub)) {
                String user = username.toLowerCase();
                sendMessage(twitchStatusHandler.followage(user), true);
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
                if(songs.drawSong().equals("notPresent")){
                    songs.drawSong();
                }
            }
            return;
        }

        if (trailing.startsWith("!song ")) {
            if (songs.getSongStatus()) {
                songs.addUser(username, trailing);
            }
            return;
        }

        if (trailing.startsWith("!song")) {
            sendMessage("@" + username + " use !jd for song lottery information, !song [number] to enter, !unsong to remove yourself", true);
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
            // TODO play with naming ideas for systems
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
        if (trailing.startsWith("!addVotes")) {
            if (commandOptionsHandler.checkAuthorization("!addVotes", username, mod, sub)) {
                try {
                    int nameBegin = trailing.indexOf(" ");
                    int nameEnd = trailing.indexOf(" ", nameBegin + 1);
                    String user = trailing.substring(nameBegin + 1, nameEnd);
                    int amt = Integer.parseInt(trailing.substring(nameEnd + 1, trailing.length()));
                    user = user.replace("@", "");
                    String userLower = user.toLowerCase();
                    spoop.addVotes(user, userLower, amt, true);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    sendEvent("Syntax: !addVotes [username] [points]");
                }
            }
            return;
        }
        if (trailing.startsWith("!remVotes")) {
            if (commandOptionsHandler.checkAuthorization("!remVotes", username, mod, sub)) {
                try {
                    int nameBegin = trailing.indexOf(" ");
                    int nameEnd = trailing.indexOf(" ", nameBegin + 1);
                    String user = trailing.substring(nameBegin + 1, nameEnd);
                    String userLower = user.toLowerCase();
                    userLower = userLower.replace("@", "");
                    user = user.replace("@", "");
                    int amt = Integer.parseInt(trailing.substring(nameEnd + 1, trailing.length()));
                    if (spoop.remVotes(userLower, amt, true)) {
                        sendMessage("Removed " + amt + " from " + user, true);
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    sendEvent("Syntax: !remVotes [username] [points]");
                }
            }
            return;
        }
        if (trailing.startsWith("!remUser")) {
            if (commandOptionsHandler.checkAuthorization("!remUser", username, mod, sub)) {
                int beginName = trailing.indexOf(" ");
                String user = trailing.substring(beginName + 1, trailing.length());
                user = user.toLowerCase();
                user = user.replace("@", "");
                spoop.remUser(user);
            }
        }
        // change the username of someone in the list, namely if they change usernames or other issues, like capitalization, etc
        if (trailing.startsWith("!changeUser")) {
            if (mod || username.equals(store.getConfiguration().joinedChannel)) {
                try {
                    int beginOld = trailing.indexOf(" ") + 1;
                    int endOld = trailing.indexOf(" ", beginOld + 1);
                    String oldName = trailing.substring(beginOld, endOld);
                    String newName = trailing.substring(endOld + 1, trailing.length());
                    spoop.changeName(oldName, newName);
                    sendMessage("Changed " + oldName + " to " + newName, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage("Syntax !changeUser [old name] [new name]", true);
                }
            }
        }

        /*
        *
        * accepts !votes or !votes [username] to cast check
         */
        if (trailing.startsWith("!votes")) {
            if (commandOptionsHandler.checkAuthorization("!votes", username, mod, sub)) {
                try {
                    // check for param of username 
                    int beginName = trailing.indexOf(" ");
                    String user = trailing.substring(beginName);
                    user = user.replace(" ", "");
                    if (user.equals("") || user.equals(" ")) {
                        user = username;
                    }
                    String userLower = user.toLowerCase();
                    userLower = userLower.replace("@", "");
                    user = user.replace("@", "");
                    int voteAmt = spoop.getVotes(userLower);
                    if (voteAmt != 0) {
                        sendMessage("@" + user + " has " + voteAmt + " votes", true);
                    } else {
                        // if this fails user doesn't have any votes
                        sendMessage("@" + user + " has no votes", true);
                    }
                } catch (IndexOutOfBoundsException ie) {
                    //if no param found, send back username request
                    username = username.replace("@", "");
                    sendMessage("@" + username + " has " + spoop.getVotes(username.toLowerCase()) + " votes", true);
                }
            }
            return;
        }
        if (trailing.toLowerCase().startsWith("!vote")) {
            if (store.getConfiguration().spoopathonStatus.equals("off")) {
                return;
            }
            // parse for params "!vote [gameID] [votes]
            System.out.println("trailing: " + trailing);
            try {
                String gameID;
                String amt = "";
                int votesToUse;
                int beginGame = trailing.indexOf(" ") + 1;
                int endGame = trailing.indexOf(" ", beginGame + 1);
                if (endGame < 0) {
                    votesToUse = -777;
                    gameID = trailing.substring(beginGame);
                } else {
                    gameID = trailing.substring(beginGame, endGame);
                    amt = trailing.substring(endGame + 1, trailing.length());
                }
                // remove any [,],(,)'s in the command usage
                gameID = gameID.replace("[", "").replace("]", "").replace("(", "").replace(")", "");
                amt = amt.replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace(" ", "");
                if (amt.equals("") || amt.equals(" ")) {
                    votesToUse = -777;
                } else {
                    amt = amt.replace("[", "").replace("]", "");
                    votesToUse = Integer.parseInt(amt);
                }
                System.out.println("game found: " + gameID + " votes found: " + votesToUse);
                if (votesToUse < 1 && votesToUse != -777) {
                    sendMessage("@" + username + " votes must be more than 0", true);
                } else {
                    spoop.useVote(username.toLowerCase(), gameID.toUpperCase(), votesToUse);
                }
            } catch (IndexOutOfBoundsException ibe) {
                sendMessage("Syntax: !vote [gameID] [votesToUse]", true);
                //ibe.printStackTrace();
            } catch (NumberFormatException ne) {
                ne.printStackTrace();
                sendMessage("Number of votes must be a whole and valid number", true);
            }
        }
        if (trailing.startsWith("!command-add")) {
            if (commandOptionsHandler.checkAuthorization("!command-add", username, mod, sub)) {
                sendMessage(commandOptionsHandler.addCommand(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-delete")) {
            if (commandOptionsHandler.checkAuthorization("!command-delete", username, mod, sub)) {
                sendMessage(commandOptionsHandler.deleteCommand(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-edit")) {
            if (commandOptionsHandler.checkAuthorization("!command-edit", username, mod, sub)) {
                sendMessage(commandOptionsHandler.editCommand(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-auth")) {
            if (commandOptionsHandler.checkAuthorization("!command-auth", username, mod, sub)) {
                sendMessage(commandOptionsHandler.authorizeCommand(username, trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-enable")) {
            if (commandOptionsHandler.checkAuthorization("!command-enable", username, mod, sub)) {
                sendMessage(commandOptionsHandler.commandEnable(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-disable")) {
            if (commandOptionsHandler.checkAuthorization("!command-disable", username, mod, sub)) {
                sendMessage(commandOptionsHandler.commandDisable(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!command-sound")) {
            if (commandOptionsHandler.checkAuthorization("!command-sound", username, mod, sub)) {
                sendMessage(commandOptionsHandler.commandSound(trailing), true);
            }
            return;
        }

        if (trailing.startsWith("!filter-all")) {
            if (commandOptionsHandler.checkAuthorization("!filter-all", username, mod, sub)) {
                sendMessage(filterHandler.getAllFilters(trailing, username), true);
            }
        }
        if (trailing.startsWith("!filter-add")) {
            if (commandOptionsHandler.checkAuthorization("!filter-add", username, mod, sub)) {
                sendMessage(filterHandler.addFilter(trailing, username), true);
            }
        }
        if (trailing.startsWith("!filter-delete")) {
            if (commandOptionsHandler.checkAuthorization("!filter-delete", username, mod, sub)) {
                sendMessage(filterHandler.deleteFilter(trailing, username), true);
            }
        }

        if (trailing.startsWith("!regex-add")) {
            if (commandOptionsHandler.checkAuthorization("!regex-add", username, mod, sub)) {
                sendMessage(filterHandler.addRegex(trailing), true);

            }
        }

        if (trailing.startsWith("!regex-edit")) {
            if (commandOptionsHandler.checkAuthorization("!regex-edit", username, mod, sub)) {
                sendMessage(filterHandler.editRegex(trailing), true);
            }
        }

        if (trailing.startsWith("!regex-del")) {
            if (commandOptionsHandler.checkAuthorization("!regex-del", username, mod, sub)) {
                sendMessage(filterHandler.delRegex(trailing), true);
            }
        }

        if (trailing.startsWith("!set-msgCache")) {
            if (commandOptionsHandler.checkAuthorization("!set-msgCache", username, mod, sub)) {
                sendMessage(pyramidDetector.setMessageCacheSize(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!set-pyramidResponse")) {
            if (commandOptionsHandler.checkAuthorization("!set-pyramidResponse", username, mod, sub)) {
                sendMessage(pyramidDetector.setPyramidResponse(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!cnt-add")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-add", username, mod, sub)) {
                sendMessage(countHandler.addCounter(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!cnt-delete")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-delete", username, mod, sub)) {
                sendMessage(countHandler.deleteCounter(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!cnt-set")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-set", username, mod, sub)) {
                sendMessage(countHandler.setCounter(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!cnt-current")) {
            if (commandOptionsHandler.checkAuthorization("!cnt-current", username, mod, sub)) {
                sendMessage(countHandler.getCurrentCount(trailing), true);
            }
            return;
        }
        if (trailing.startsWith("!countadd")) {
            if (commandOptionsHandler.checkAuthorization("!countadd", username, mod, sub)) {
                sendMessage(countHandler.updateCount(trailing), true);
            }
            return;
        }

        if (trailing.startsWith("!totals")) {
            if (commandOptionsHandler.checkAuthorization("!totals", username, mod, sub)) {
                sendMessage(countHandler.totals(), true);
            }
            return;
        }
        // special case command due to action discrepency
        if (username.equalsIgnoreCase("buttgasm") && trailing.startsWith("!gotem")) {
            sendMessage(commandOptionsHandler.parseForUserCommands(trailing, username, mod, sub), false);
        }
        sendMessage(commandOptionsHandler.parseForUserCommands(trailing, username, mod, sub), true);
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
            //auto set level for broadcaster to 600 level
        } else {
            for (int i = 0; i < store.getEditors().size(); i++) {
                final ConfigParameters.Editor editor = store.getEditors().get(i);
                if (editor.username.equalsIgnoreCase(user)) {
                    level = editor.level;
                }
            }
        }
        sendEditorMessage(editorCommandHandler.parseForCommand(user, level, msg, channel));
    }

    public void handleWhisper(String user, String msg) {
        System.out.println("Whisper from " + user + ", message: " + msg);
    }

    /**
     * Deal with raid message
     *
     * @param msg raw incoming IRC see example
     */
    public void handleRaid(String msg) {
        // TODO replicate the sub response system for raids and bits
        // parse raw message
        try {
            String raider = messageTagValue(msg, "msg-param-displayName=");
            int viewers = Integer.parseInt(messageTagValue(msg, "msg-param-viewerCount="));
            /// send to eventhandler for message to chat

            if (store.getConfiguration().raidReply.equals("on")) {
                eHandler.handleRaid(raider, viewers);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendEvent("Error occured in handle raid");

        }
    }

    static String tempName = "";
    static int tempGiftAmount = 0;

    public void handleUserNotice(String msg) {
        // TODO add ability for different responses for different months
        try {
            String giftRecipient = "";
            String subMonths = "0";
            boolean massGifted = false;
            boolean gifted = false;
            boolean prime = false;

            /*
        ** catch "msg-param-mass-gift-count"  before looking for sub length
        ** set temp variable if "display-name" = name of mass gifter for count # of times, don't respond
        ** new sub = 1 month
             */
            String subDisplayName = messageTagValue(msg, "display-name=");
            // check for gifted batch sub
            if (msg.contains("msg-id=submysterygift")) {
                // set these variables on a new sub batch
                tempGiftAmount = Integer.parseInt(messageTagValue(msg, "gift-count="));
                tempName = subDisplayName;
                massGifted = true;
            } else if (msg.contains("msg-id=subgift")) {
                subMonths = messageTagValue(msg, "msg-param-months=");
                giftRecipient = messageTagValue(msg, "recipient-display-name=");
                gifted = true;
                if (tempName.equals(subDisplayName)) {
                    tempGiftAmount--;
                } else {
                    tempGiftAmount = 1;
                }
                if (tempGiftAmount < 0) {
                    tempName = "";
                }
            } else {
                // sub gift notification does not have this param
                subMonths = messageTagValue(msg, "msg-param-cumulative-months=");

            }

            if (gifted && tempName.equals(subDisplayName)) {

                // send to event list
                sendEvent(giftRecipient + " has been gifted a sub by " + subDisplayName);

                // exit and return if this is true- prevents spammed replies 
                // (mass sub gifts, ie 100 sub bomb)
            } else {
                String subTier = messageTagValue(msg, "msg-param-sub-plan=");
                int subPoints = 0;
                if (subTier.equals("Prime")) {
                    subPoints = 1;
                    prime = true;
                } else if (Integer.parseInt(subTier) == 1000) {
                    subPoints = 1;
                } else if (Integer.parseInt(subTier) == 2000) {
                    subPoints = 2;
                } else if (Integer.parseInt(subTier) == 3000) {
                    subPoints = 6;
                }
                if (subPoints == 0) {
                    subPoints = 1;
                }
                //send to sub method in CountHandler.java get String and send to chat
                if (store.getConfiguration().subReply.equals("on")) {
                    sendMessage(CountHandler.SubHandler.handleSubMessage(massGifted, gifted, subDisplayName, giftRecipient, tempGiftAmount, subPoints, prime, subMonths), true);
                }
                if (subPoints != 0 && (Integer.parseInt(subMonths) < 2 || prime)) {
                    //TODO options for different values for new versus resub value towards marathon
                    if (store.getConfiguration().marathonStatus.equals("on")) {
                        mHandler.addSub(subPoints, massGifted, gifted, tempGiftAmount);
                    }
                }
                // send to spoopathon system if on
                if (store.getConfiguration().spoopathonStatus.equals("on")) {
                    // drop to lowercase for uniformity when checking for people
                    String subLower = subDisplayName.toLowerCase();
                    if (massGifted) {
                        spoop.handleSubGift(subDisplayName, subLower, subPoints, tempGiftAmount);
                    } else if (!gifted) {
                        spoop.handleSub(subDisplayName, subLower, subPoints);
                    } else if (gifted) {
                        spoop.handleSingleGift(subDisplayName, subLower, subPoints);
                    }
                }
            }
        } catch (Exception e) {
            sendEvent("Error occured witin usernotice");

            LOGGER.log(Level.SEVERE, Arrays.toString(e.getStackTrace()));
        }
    }

    public void handleBits(String msg) {
        try {
            //TODO replicate sub response system 
            // add ability to have different or min amount for message
            String amt = messageTagValue(msg, "bits=");
            String user = messageTagValue(msg, "display-name=");

            if (store.getConfiguration().marathonStatus.equals("on")) {
                //mHandler.addBits(Integer.parseInt(amt));
            }

            // send to spoopathon system if on
            if (store.getConfiguration().spoopathonStatus.equals("on")) {
                String lowerUser = user.toLowerCase();
                spoop.handleBits(user, lowerUser, Integer.parseInt(amt));
            }

            //send to event handler
            if (store.getConfiguration().bitReply.equals("on")) {
                eHandler.handleBits(user, Integer.parseInt(amt));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendEvent("Error occured in handle bits");
        }
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

            //Handle reconnet notice ":tmi.twitch.tv RECONNECT"
            if (msg.equals(":tmi.twitch.tv RECONNECT")) {
                System.out.println("RECONNECT notice received, restarting bot");
                sendEvent("RECONNECT notice received, attempting to reconnect");
                DashboardController.wIRC.close();
            }

            boolean isMod = false;
            boolean isSub = false;
            String username = "";
            String chanFind = msg;
            String sendRaw = msg;
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

            // Find the VIP indication, treat at sub level
            if (msg.contains("@badges=vip/1,")) {
                isSub = true;
            }
            // Find the subscriber indication
            final int subPosition = msg.indexOf("subscriber=") + 11;
            if ("1".equals(msg.substring(subPosition, subPosition + 1))) {
                isSub = true;
            }

            if (msg.contains("user-type=")) {
                int usernameStart = msg.indexOf(":", msg.indexOf("user-type="));
                int usernameEnd = msg.indexOf("!", usernameStart);
                if (usernameStart != -1 && usernameEnd != -1) {
                    username = msg.substring(usernameStart + 1, usernameEnd).toLowerCase();
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
            int channelPosition = 0;
            if (msg.contains("#")) {
                channelPosition = msg.indexOf("#");
            } else if (msg.contains(".tmi.twitch.tv WHISPER")) {
                channelPosition = 0;
            } else {
                return;
            }
            if (msgPosition == -1) {
                return;
            }

            // Ensure we can find "PRIVMSG" as an indication that this is a
            // user message, make sure we only search a limited bound, because
            // somebody can potentially fake a mod by including "PRIVMSG" 
            // in their message
            if (channelPosition > 0) {

                final String hasPrivMsg = msg.substring(0, channelPosition);
                final int privMsgIndex = hasPrivMsg.indexOf("PRIVMSG");
                if (privMsgIndex == -1 || sendRaw.contains("bits=")) {
                    if (hasPrivMsg.contains("USERNOTICE")) {
                        // split into raid versus sub
                        if (sendRaw.contains("sub-plan")) {
                            //sub usernotice
                            handleUserNotice(sendRaw);
                        } else if (sendRaw.contains("msg-param-viewerCount")) {
                            //incoming raid
                            handleRaid(sendRaw);
                        } else if (sendRaw.contains("msg-id=giftpaidupgrade")) {
                            //incoming subtember 2018 promo
                            //ignore until needed
                            //handlePromo(sendRaw);
                        } else if (sendRaw.contains("bits=")) {
                            handleBits(sendRaw);
                        } else {
                            //ignore other USERNOTICE for now
                        }
                        return;
                    }
                    if (sendRaw.contains("bits=")) {
                        handleBits(sendRaw);
                        return;
                    }
                    return;
                }

                // Capture the raw message, and find the message used
                final int msgIndex = msg.indexOf(":", channelPosition);

                // No message found, return immediately
                if (msgIndex == -1) {
                    return;
                }

                msg = msg.substring(msgIndex + 1);

                // Catch msg-id for use in the moderation handler for .delete msgID
                final int msgIdBegin = sendRaw.indexOf("id=") + 3;
                final int msgIdEnd = sendRaw.indexOf(";", msgIdBegin);
                String msgId = sendRaw.substring(msgIdBegin, msgIdEnd);

                // Determine where message is from
                final int channelName = chanFind.indexOf("#", chanFind.indexOf("PRIVMSG"));
                final int chanIndex = chanFind.indexOf(" ", channelName);
                String channel = chanFind.substring(channelName + 1, chanIndex);
                String botName = store.getConfiguration().account;

                // filter system access via bot channel's chat
                if (channel.equalsIgnoreCase(botName)) {
                    handleEditorCommand(username, msg, channel);
                } else {

                    // Handle the message
                    // Don't worry about this >.>
                    if (guiHandler.bot.rogue) {
                        Random rng = new Random();
                        if (rng.nextInt(100) < 40) {
                            ConfigurationController.goRogue.sassCommand(isMod);
                        } else {
                            handleCommand(username, isMod, isSub, msg, msgId);
                        }
                    } else {
                        handleCommand(username, isMod, isSub, msg, msgId);
                    }
                }
            } else {
                //handle an incoming whisper
                int whisperBegin = msg.indexOf("WHISPER") + 17;
                msg = msg.substring(whisperBegin);
                handleWhisper(username, msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Error detected in parsing a message: throwing away message ", e.getMessage());
        }
    }

    //Method to add events to the GUI event list
    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                store.getEventList().addList(event);
            }
        });
    }

    // Extract value from IRC tags
    private String messageTagValue(String message, String tag) {
        int startIndex = message.indexOf(tag) + tag.length();
        int endIndex = message.indexOf(";", startIndex);
        String tagValue = message.substring(startIndex, endIndex);

        return tagValue;
    }
    
    public static class testUnit {
        public testUnit(){
            
        }
        public boolean testString(String test){
            System.out.println("Testing :" + test);
            return test.equals("test");
        }
    }
    
}
