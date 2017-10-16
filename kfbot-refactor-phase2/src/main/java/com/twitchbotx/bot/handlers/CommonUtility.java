package com.twitchbotx.bot.handlers;

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
}
