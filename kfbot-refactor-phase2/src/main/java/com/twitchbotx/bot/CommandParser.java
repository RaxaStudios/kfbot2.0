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
        twitchStatusHandler = new TwitchStatusHandler(store);
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
        System.out.println("TRAIL: " + trailing);
        //
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
                /*CommonUtility.ERRORLOGGER.severe("SEVERE MESSAGE TEST");
                CommonUtility.writeError("Example of a caught exception :"
                        + "\n"
                        + "Exception in thread \"pool-2-thread-1\" java.lang.IllegalStateException: Not on FX application thread; currentThread = pool-2-thread-1\n"
                        + "    at com.sun.javafx.tk.Toolkit.checkFxUserThread(Toolkit.java:236)\n"
                        + "    at com.sun.javafx.tk.quantum.QuantumToolkit.checkFxUserThread(QuantumToolkit.java:423)\n"
                        + "    at javafx.scene.Parent$2.onProposedChange(Parent.java:367)\n"
                        + "    at com.sun.javafx.collections.VetoableListDecorator.setAll(VetoableListDecorator.java:113)\n"
                        + "    at com.sun.javafx.collections.VetoableListDecorator.setAll(VetoableListDecorator.java:108)\n"
                        + "    at com.sun.javafx.scene.control.skin.LabeledSkinBase.updateChildren(LabeledSkinBase.java:575)");*/
            } catch (Exception e) {
                //Thread.dumpStack();
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
            //LOGGER.log(Level.INFO, "{0} {1} {2}", new Object[]{username, mod, sub});
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
                songs.drawSong();
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
                    //System.out.println("user:" + user);
                    user = user.replace("@", "");
                    String userLower = user.toLowerCase();
                    spoop.addVotes(user, userLower, amt, true);
                    // double send with spoop.addVotes thing
                    //sendMessage("Added " + amt + " votes to " + user);
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
                    //System.out.println("Old found:" + oldName + " new: " + newName);
                    spoop.changeName(oldName, newName);
                    sendMessage("Changed " + oldName + " to " + newName, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage("Syntax !changeUser [old name] [new name]", true);
                }
            }
        }
        /*
        !addVotes sending twice to chat for adding points/votes FIXED
        sub system adding 1 point instead of 5 like it should FIXED
        sub system not adding to certain users, possibly capitalization issue FIXED
        !votes not sending anything if user doesn't have votes FIXED
        sub gifted points aren't adding to user correctly possible capitalization issue
        bits not working with spoopathon system
        double check bit/raid replies work
         */

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
            if(store.getConfiguration().spoopathonStatus.equals("off")){ return; }
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

//        if (trailing.startsWith("!commands")) {
//            if (commandOptionsHandler.checkAuthorization("!commands", username, mod, sub)) {
//                commandOptionsHandler.commands(username, mod, sub);
//            }
//        }
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
        if (username.equalsIgnoreCase("buttgasm") && trailing.startsWith("!gotem")) {
            sendMessage(commandOptionsHandler.parseForUserCommands(trailing, username, mod, sub), false);
        }
        //System.out.println("trailing: " + trailing);
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
        // todo enable this system
        sendEditorMessage(editorCommandHandler.parseForCommand(user, level, msg, channel));
    }

    public void handleWhisper(String user, String msg) {
        System.out.println("Whisper from " + user + ", message: " + msg);
    }

    /**
     * Deal with raid message
     *
     * @param msg raw incoming IRC see example
     *
     * example:
     * @badges=<badges>;color=<color>;display-name=<display-name>;emotes=<emotes>;id=<id-of-msg>;login=<user>;mod=<mod>;msg-id=<type-of-msg>;msg-param-displayName=<msg-param-displayName>;msg-param-login=<msg-param-login>;msg-param-viewerCount=<msg-param-viewerCount>;room-id=<room-id>;subscriber=<subscriber>;system-msg=<system-msg>;tmi-sent-ts=<timestamp>;turbo=<turbo>;user-id=<user-id>;user-type=<user-type>
     * :tmi.twitch.tv USERNOTICE #<channel> :<message>
     */
    public void handleRaid(String msg) {
        // TODO replicate the sub response system for raids and bits
        // parse raw message
        try {
            String raider;
            int viewers;

            int beginRaider = msg.indexOf("msg-param-displayName=") + 22;
            int endRaider = msg.indexOf(";", beginRaider);
            raider = msg.substring(beginRaider, endRaider);
            int beginViewers = msg.indexOf("msg-param-viewerCount=") + 22;
            int endViewers = msg.indexOf(";", beginViewers);
            viewers = Integer.parseInt(msg.substring(beginViewers, endViewers));
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
            //temporary spam prevention measure for overflow of secondary sharing of sub tenure
            if (msg.contains("msg-param-stream-months") || msg.contains("msg-param=cumulative-months") || msg.contains("msg-param-should-share-streak")) {
                return;
            }

            String giftRecipient = "";

            String subMonths = "0";
            boolean massGifted = false;
            boolean gifted = false;
            boolean prime = false;

            //System.out.println("USERNOTICE FOUND msg=" + msg);
            /*
        ** catch "msg-param-mass-gift-count"  before looking for sub length
        ** set temp variable if "display-name" = name of mass gifter for count # of times, don't respond
        ** new sub = 1 month
        ** example msg: 
        **@badges=subscriber/0,premium/1;color=#FF69B4;display-name=MutantLittle;emotes=;id=570922ab-358d-4d65-a312-89e26e7e0d9d;login=mutantlittle;mod=0;msg-id=sub;msg-param-months=1;msg-param-sub-plan-name=The\sFruit\sBasket\s:D;msg-param-sub-plan=Prime;room-id=59712498;subscriber=0;system-msg=MutantLittle\sjust\ssubscribed\swith\sTwitch\sPrime!;tmi-sent-ts=1524967138517;turbo=0;user-id=92690807;user-type= :tmi.twitch.tv USERNOTICE #kungfufruitcup
             */
            int beginDisplayName = msg.indexOf("display-name=") + 13;
            int endDisplayName = msg.indexOf(";", beginDisplayName);
            String subDisplayName = msg.substring(beginDisplayName, endDisplayName);
            // check for gifted batch sub
            if (msg.contains("msg-id=submysterygift")) {
                int beginGiftAmount = msg.indexOf("gift-count=") + 11;
                int endGiftAmount = msg.indexOf(";", beginGiftAmount);
                // set these variables on a new sub batch
                tempGiftAmount = Integer.parseInt(msg.substring(beginGiftAmount, endGiftAmount));
                tempName = subDisplayName;
                massGifted = true;
                gifted = false;
            } else {
                // sub gift notification does not have this param
                int beginMonths = msg.indexOf("msg-param-cumulative-months=") + 28;
                int endMonths = msg.indexOf(";", beginMonths);
                subMonths = msg.substring(beginMonths, endMonths);
                massGifted = false;
                //System.out.println(subMonths);
                if (msg.contains("msg-id=subgift")) {
                    int beginRecip = msg.indexOf("recipient-display-name=") + 23;
                    int endRecip = msg.indexOf(";", beginRecip);
                    giftRecipient = msg.substring(beginRecip, endRecip);
                    gifted = true;
                    if (tempName.equals(subDisplayName)) {
                        tempGiftAmount--;
                    } else {
                        tempGiftAmount = 1;
                    }
                    if (tempGiftAmount < 0) {
                        tempName = "";
                    }
                    /* if (store.getConfiguration().subReply.equals("on")) {
                        sendMessage(CountHandler.SubHandler.handleSubMessage(massGifted, gifted, subDisplayName, giftRecipient, tempGiftAmount, tempGiftAmount, prime, subMonths), true);
                    }
                    if (store.getConfiguration().marathonStatus.equals("on")) {
                        mHandler.addSub(1, massGifted, gifted, tempGiftAmount);
                    }*/
                } else {
                    giftRecipient = "";
                    gifted = false;
                }
            }

            if (gifted && tempName.equals(subDisplayName)) {

                // send to event list
                sendEvent(giftRecipient + " has been gifted a sub by " + subDisplayName);

                // exit and return if this is true- prevents spammed replies
            } else {

                int beginTier = msg.indexOf("msg-param-sub-plan=") + 19;
                int endTier = msg.indexOf(";", beginTier);
                String subTier = msg.substring(beginTier, endTier);
                //System.out.println(subTier);
                prime = false;
                int subPoints = 0;
                if (subTier.equals("Prime")) {
                    subPoints = 1;
                    prime = true;
                } else if (Integer.parseInt(subTier) == 1000) {
                    subPoints = 1;
                    prime = false;
                } else if (Integer.parseInt(subTier) == 2000) {
                    subPoints = 2;
                } else if (Integer.parseInt(subTier) == 3000) {
                    subPoints = 6;
                }
                if (subPoints == 0) {
                    subPoints = 1;
                }
                //send to sub method in CountHandler.java get String and send to chat
                // if disabled do not send this message or send to parser
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
                //System.out.println("sending to spoopathon. name:" + subDisplayName + " subpoints:" + subPoints + " massGift:" + massGifted + " gifted:" + gifted);
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
        /*
        ** example msg:
        **@badges=subscriber/0,bits/100;bits=100;color=;display-name=King_of_Death_;emotes=9:68-69,71-72;id=0e5c1c5d-cc9a-4d36-be6b-2224c285b674;mod=0;room-id=59712498;subscriber=1;tmi-sent-ts=1524965816604;turbo=0;user-id=107746834;user-type= :king_of_death_!king_of_death_@king_of_death_.tmi.twitch.tv PRIVMSG #kungfufruitcup :cheer100 I'm gonna go stream some ME2, I will be back later to lurk <3 <3
         */
        try {
            //TODO replicate sub response system 
            // add ability to have different or min amount for message
            int beginAmt = msg.indexOf("bits=") + 5;
            int endAmt = msg.indexOf(";", beginAmt);
            String amt = msg.substring(beginAmt, endAmt);
            //System.out.println(amt);

            int beginName = msg.indexOf("display-name=") + 13;
            int endName = msg.indexOf(";", beginName);
            String user = msg.substring(beginName, endName);
            if (store.getConfiguration().marathonStatus.equals("on")) {
                mHandler.addBits(Integer.parseInt(amt));
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
            /* if(msg.contains("@badges=vip/1,")){
                isSub = true;
            }*/
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
                    //System.out.println(username + " USERNAME");
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
                    //check for "USERNOTICE" alternate message
                    if (hasPrivMsg.contains("USERNOTICE")) {
                        // split into raid versus sub
                        if (sendRaw.contains("sub-plan")) {
                            //sub usernotice
                            handleUserNotice(sendRaw);
                        } else if (sendRaw.contains("msg-param-viewerCount")) {
                            //incoming raid
                            handleRaid(sendRaw);
                        } else if (sendRaw.contains("msg-id=giftpaidupgrade")) {
                            //incoming subtember promo
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
                // filter system access via bot channel's chat
                final int channelName = chanFind.indexOf("#", chanFind.indexOf("PRIVMSG"));
                final int chanIndex = chanFind.indexOf(" ", channelName);
                String channel = chanFind.substring(channelName + 1, chanIndex);
                String botName = store.getConfiguration().account;
                //System.out.println(channel + " " + botName);

                if (channel.equalsIgnoreCase(botName)) {
                    handleEditorCommand(username, msg, channel);
                } else {

                    // Handle the message
                    //System.out.println("un: " + username + " msg: " + msg);
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
