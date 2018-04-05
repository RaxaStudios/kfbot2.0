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
            "!uptime",
            "!followage",
            "!highlight",
            "!commands",
            "!command-add",
            "!command-delete",
            "!command-enable",
            "!command-disable",
            "!command-edit",
            "!command-auth",
            "!command-repeat", // TODO later.
            "!command-delay",
            "!command-interval",
            "!command-cooldown",
            "!command-sound",
            "!command-add-sub",
            "!command-edit-sub",
            "!command-delete-sub",
            "!command-auth-sub",
            "!command-cooldown-sub",
            "!command-sound-sub",
            "!set-msgCache",
            "!set-pyramidResponse",
            "!cnt-add",
            "!cnt-delete",
            "!cnt-set",
            "!cnt-current",
            "!count",
            "!filter-all",
            "!filter-add",
            "!filter-delete",
            "!phrase-add",
            "!phrase-delete",
            "!phrase-timeout",
            "!countadd",
            "!totals",
            "!countadd",
            "!addPoints",
            "!setTime",
            "!start",
            "!totalTime",
            "!setDonationPoints",
            "!addDonationPoints",
            "!donation-add",
            "!donation-delete",
            "!dwpoints",
            "!lottery-open",
            "!lottery-clear",
            "!draw",
            "!s-game-add",
            "!s-game-delete",
            "!s-set-name",
            "!s-set-points",
            "!s-addPoints",
            "!points",
            "!s-status",
            "!song-open",
            "!song-close",
            "!song-draw",
            "!song"
            
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
