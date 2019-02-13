package com.twitchbotx.bot;

/**
 * This class is a singleton, meaning there is only one instantiation of it in the entire program.
 */
public final class Commands {

    private static Commands instance = null;
    protected Commands() {
        // Exists only to defeat instantiation.
    }
    public static Commands getInstance() {
        if(instance == null) {
            instance = new Commands();
        }
        return instance;
    }

    private String[] reservedCommands = {
        
        //UPDATE COMMAND LIST FOR NON EDITABLE COMMANDS 
        // TODO
            "!uptime",
            "!followage",
            "!highlight",
            "!command-add",
            "!command-delete",
            "!command-enable",
            "!command-disable",
            "!command-edit",
            "!command-auth",
            "!command-repeat",
            "!command-delay",
            "!command-interval",
            "!command-cooldown",
            "!command-sound",
            "!filter-all",
            "!filter-add",
            "!filter-delete",
            "!phrase-add",
            "!phrase-delete",
            "!phrase-timeout"

            
    };

    /**
     * This method checks whether a command has already been reserved.
     *
     * @param command The command to check for.
     *
     * @return True - the command has been reserved and we shouldn't override it
     * False - the command has not been reserved and we can override it
     */
    public boolean isReservedCommand(final String command) {
        for (String reservedCommand : reservedCommands) {
            if (command.contentEquals(reservedCommand)) {
                return true;
            }
        }
        return false;
    }

}
