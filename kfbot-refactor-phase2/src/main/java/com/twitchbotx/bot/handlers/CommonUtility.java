package com.twitchbotx.bot.handlers;

import com.twitchbotx.gui.guiHandler;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javafx.application.Platform;

public final class CommonUtility {

    /**
     * This method checks the arguments for a particular command and the
     * arguments needed and returns the parameters.
     *
     * It simply throws an exception if the arguments defined are not the ones
     * that are needed.
     *
     * @param cmd The command for the request
     *
     * @param input A given input command for the request
     *
     * @param paramRequired True - additional parameters are needed False - no
     * additional parameters are needed
     *
     * @return The additional parameters
     *
     * @throws IllegalArgumentException An exception if the user never input all
     * the parameters
     */
    public static String getInputParameter(String cmd, String input, boolean paramRequired)
            throws IllegalArgumentException {
        if (input.length() == cmd.length()) {
            if (paramRequired) {
                throw new IllegalArgumentException();
            }
            return "";
        }
        return input.substring(cmd.length() + 1);
    }

    // hold static logger here for access
    public static final Logger ERRORLOGGER = Logger.getLogger(CommonUtility.class.getSimpleName());
    
    /**
     * This method will all for errors to be saved to a file for later
     * inspection from client side use
     *
     * @param msg -> error caught anywhere in the program
     *
     */
    public static void writeError(String msg) {
        //try {
            /*try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("writePath", true)))) {
                out.println("the text");
            }
            Files.write(Paths.get("errors.log"), msg.getBytes(), StandardOpenOption.APPEND);*/
            ERRORLOGGER.info(msg);
        /*} catch (IOException ie) {
            ie.printStackTrace();
            String event = "Error occurred trying to write lottery to file";
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    guiHandler.bot.getStore().getEventList().addList(event);
                }
            });
        }*/
    }
}
