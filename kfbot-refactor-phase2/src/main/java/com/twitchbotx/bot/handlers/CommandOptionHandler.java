package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.CommandParser;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.twitchbotx.bot.Commands;
import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.w3c.dom.DOMException;

/**
 * This class is a command handler for most of the common commands in this bot.
 */
public final class CommandOptionHandler {

    /*
        ** If command = !filter-all & username is valid & mod requirement is met per XML & sub requirement is met per XML
        ** Then create a moderationHandler and send "trailing" to the handleTool method
        ** filter, command, set broadcaster only
        ** See XML for other requirements
     */
    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final Datastore store;

    /**
     * This is a simple constructor for the command handler.
     *
     * It will use elements to write/rewrite the XML as storage.
     *
     * @param store The element references to the XML data
     */
    public CommandOptionHandler(final Datastore store) {
        this.store = store;
    }

    public String parseForUserCommands(String msg, String username, boolean mod, boolean sub) {
        for (int i = 0; i < store.getCommands().size(); i++) {
            try {
                final ConfigParameters.Command command = store.getCommands().get(i);

                int endOfCmd = msg.indexOf(" ");
                if (endOfCmd == -1) {
                    endOfCmd = msg.length();
                }
                String cmd = msg.substring(0, endOfCmd);
                if (cmd.contentEquals(command.name)) {
                    if (!checkAuthorization(cmd, username, mod, sub)) {
                        return "";
                    }
                    if (command.disabled) {
                        return "";
                    }
                    String sendTxt;
                    if (msg.contains(" ")) {
                        String param = msg.substring(endOfCmd + 1);
                        sendTxt = command.text.replace("%param%", param);
                    } else {
                        sendTxt = command.text;
                    }
                    if (sendTxt.contains("%param%")) {
                        return cmd + " requires a parameter.";
                    }
                    if (sendTxt.contains("@")){
                        sendTxt = sendTxt.replace("@","");
                    }
                    //check for follow command optional %game% variable
                    if(sendTxt.contains(("%game%")) && command.name.equals("!follow")){
                        String user = msg.substring(endOfCmd + 1);
                        sendTxt = sendTxt.replaceAll("%game%", CommandParser.twitchStatusHandler.getLastGame(user));
                    }
                    
                    if (!username.contentEquals(store.getConfiguration().joinedChannel)) {
                        Calendar calendar = Calendar.getInstance();
                        Date now = calendar.getTime();
                        
                        //check for blank cdUntil
                        Long cooldown;
                        try {
                            cooldown = Long.parseLong(command.cdUntil);
                        } catch (NumberFormatException nfe) {
                            cooldown = (Long) new Date(now.getTime()).getTime();
                            nfe.printStackTrace();
                        }

                        Date cdTime = new Date(cooldown);

                        if (now.before(cdTime)) {
                            return "";
                        }
                        Long cooldownInSec = new Long(0);
                        try {
                            cooldownInSec = Long.parseLong(command.cooldownInSec);
                        } catch (NumberFormatException ne) {
                            //set value of cooldownInSec to 0
                            store.setUserCommandAttribute(command.name, "cooldownInSec", "0", true);
                            ne.printStackTrace();
                        }
                        cdTime = new Date(now.getTime() + cooldownInSec * 1000L);
                        store.updateCooldownTimer(command.name, cdTime.getTime());

                    }
                    if (!command.sound.isEmpty()) {
                        playSound(command.sound);
                    }
                    if (sendTxt.isEmpty()) {
                        return "";
                    } else {
                        return sendTxt;
                    }
                }
            } catch (DOMException | NumberFormatException e) {
                LOGGER.severe(e.toString());
                e.printStackTrace();
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

        return "";
    }

    /**
     * This method will add a new command to the bot.
     *
     * @param msg The message from the user
     */
    public String addCommand(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-add", msg, true);
            final int separator = parameters.indexOf(" ");
            final String cmd = parameters.substring(0, separator);
            final String txt = parameters.substring(separator + 1);

            if (Commands.getInstance().isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }

            if (!cmd.startsWith("!")) {
                return "Commands should start with an !";
            }

            final boolean added = store.addCommand(cmd, txt);
            if (added) {
                return "Added command [" + cmd + "] : [" + txt + "]";
            } else {
                return "Command [" + cmd + "] already exists!";
            }

        } catch (IllegalArgumentException e) {
            LOGGER.warning("Unable to add command");
        }
        return "Syntax: !command-add [!command] [text].";
    }

    /**
     * This method deletes an existing command.
     *
     * @param msg The message from the user
     */
    public String deleteCommand(final String msg) {
        final String cmd = CommonUtility.getInputParameter("!command-delete", msg, true);
        if (Commands.getInstance().isReservedCommand(cmd)) {
            return "Failed: [" + cmd + "] is a reserved command.";
        }

        boolean deleted = store.deleteCommand(cmd);
        if (deleted) {
            return "Command [" + cmd + "] deleted.";
        }

        return "Command [" + cmd + "] not found.";
    }

    /**
     * This function will edit a command.
     *
     * @param msg The message from the user
     */
    public String editCommand(final String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-edit", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String txt = parameters.substring(separator + 1);
            if (txt.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (Commands.getInstance().isReservedCommand(cmd)) {
                return "Failed: [" + cmd + "] is a reserved command.";
            }

            final boolean edited = store.editCommand(cmd, txt);
            if (edited) {
                return "Command [" + cmd + "] changed to " + txt;
            }

            return "Command [" + cmd + "] not found.";
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-edit [!command] [text].";
        }
    }

    /**
     * This method will edit the authority of this command. Typically it grants
     * different access to different people for a particular command.
     *
     * @param username The username of the person getting authority over the
     * command
     *
     * @param msg The message from the user
     */
    public String authorizeCommand(String username, String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-auth", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String auth = parameters.substring(separator + 1) + " ";
            if (Commands.getInstance().isReservedCommand(cmd)) {
                if (!username.contentEquals(store.getConfiguration().joinedChannel)) {
                    return "Failed: only the channel owner can edit the auth for reserved commands.";
                }
            }
            if (store.setUserCommandAttribute(cmd, "auth", auth, true)) {
                return "Command [" + cmd + "] authorization set to [" + auth + "]";
            }
        } catch (IllegalArgumentException e) {
            return "Syntax: !command-auth [!command] [auth list].";
        }

        return "Syntax: !command-auth [!command] [auth list].";
    }

    /**
     * Appends a sounds file to command node
     *
     * @param msg The message from the user
     * @return string with confirmation
     */
    public String commandSound(String msg) {
        try {
            final String parameters = CommonUtility.getInputParameter("!command-sound", msg, true);
            int separator = parameters.indexOf(" ");
            String cmd = parameters.substring(0, separator);
            String soundFile = parameters.substring(separator + 1);
            if (soundFile.contentEquals("null")) {
                soundFile = "";
            }
            if (store.setUserCommandAttribute(cmd, "sound", soundFile, false)) {
                return "Command [" + cmd + "] set to play sound file [" + soundFile + "]";
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Error detected in command sound");
        }

        return "Syntax: !command-sound [!command] [filename.wav]";
    }

    /**
     * Disable a particular command.
     *
     * @param msg The message from the user
     */
    public String commandDisable(final String msg) {
        try {
            String cmd = CommonUtility.getInputParameter("!command-disable", msg, true);
            if (store.setUserCommandAttribute(cmd, "disabled", "true", false)) {
                return "Command " + cmd + " disabled.";
            }
        } catch (IllegalArgumentException e) {
            return "Unknown error occurred trying to disable command";
        }

        return "Syntax: !command-disable [!command]";
    }

    public String commandEnable(String msg) {
        try {
            String cmd = CommonUtility.getInputParameter("!command-enable", msg, true);
            if (store.setUserCommandAttribute(cmd, "disabled", "false", false)) {
                return "Command " + cmd + " enabled.";
            }
        } catch (IllegalArgumentException e) {
            return "Unknown error occurred trying to enable command";
        }

        return "Syntax: !command-enable [!command]";
    }

    public boolean checkAuthorization(String userCommand, String username, boolean mod, boolean sub) {
        String auth = "";
        int authLvl = 0;
        if (username.contentEquals(store.getConfiguration().joinedChannel)) {
            return true;
        }
        for (int i = 0; i < store.getCommands().size(); i++) {
            final ConfigParameters.Command command = store.getCommands().get(i);
            if (userCommand.contentEquals(command.name)) {
                auth = command.auth;
                authLvl = command.authLvl;
                break;
            }
        }
        if (auth.isEmpty()) {
            System.out.println("false auth");
            return false;
        }
        if (auth.toLowerCase().contains("-" + username)) {
            System.out.println("false auth");
            return false;
        }
        if (auth.toLowerCase().contains("+" + username)) {
            System.out.println("true auth");
            return true;
        }
        //check for authorization level of user against any values in the command
        if (authLvl < findAuth(username)) {
            System.out.println("true auth with level");
            return true;
        }
        if ((auth.contains("-m")) && mod) {
            LOGGER.info("MOD FALSE: ");
            System.out.println("false auth");
            return false;
        }
        if ((auth.contains("+m")) && mod) {
            LOGGER.info("MOD TRUE: ");
            System.out.println("true auth");
            return true;
        }
        if ((auth.contains("-s")) && sub) {
            System.out.println("false auth");
            return false;
        }
        if ((auth.contains("+s")) && sub) {
            System.out.println("true auth");
            return true;
        }
        if (auth.contains("-a")) {
            System.out.println("false auth");
            return false;
        }
        if (auth.contains("+a")) {
            System.out.println("true auth");
            return true;
        }
        return false;
    }

    private int findAuth(String username) {
        int lvl = 0;
        List<ConfigParameters.Editor> editors = store.getEditors();
        for (ConfigParameters.Editor editor : editors) {
            if (username.equals(editor.username)) {
                if (String.valueOf(editor.level).equals(" ")) {
                    lvl = 0;
                } else {
                    lvl = editor.level;
                }
            }
        }
        // if user is not found user is not an editor, there auth lvl 0
        // TODO add in levels for admin, editor, mod, sub, etc
        return lvl;
    }
    
    /**
     * Plays sound file based on attached .wav to certain commands within
     * sound="" in XML. This will need to be patched out as of java 9 the
     * sun.audio API is unavailable
     *
     * @param file
     */
    private void playSound(String file) {
        try {
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve(file);
            Media hit = new Media(xmlResolved.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
