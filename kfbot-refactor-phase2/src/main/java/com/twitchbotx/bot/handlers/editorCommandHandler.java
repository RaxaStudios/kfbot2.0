/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

// TODO permission levels 
import com.twitchbotx.gui.guiHandler;

// bot join own channel for control functions/commands
// hardcode permissions for editors/admins/broadcaster 
// into startup process and in xml
/*
            ** 0-99 unable to use bot
            ** 100-199 nonsub
            ** 200-299 sub
            ** 300-399 moderator
            ** 400-499 editor
            ** 500-599 admin
            ** 600 broadcaster
 */
public class editorCommandHandler {

    // For handling all normal commands
    private final static CommandOptionHandler commandOptionsHandler = new CommandOptionHandler(guiHandler.bot.getStore());
    // For filter handling
    public static FilterHandler filterHandler = new FilterHandler(guiHandler.bot.getStore());

    public static String parseForCommand(String user, int level, String msg, String channel) {
        System.out.println("message coming from bot's channel. user:" + user + " lvl:" + level + " msg:" + msg);
        if (level < 400) {
            return "";
        } else {
            /**
             * available editor commands: !filter-add !filter-delete !regex-add
             * !regex-edit !regex-del
             *
             */
            return handleInput(msg, user);
        }
        //return "";
    }

    public static String handleInput(String trailing, String username) {

        // mimic CommandParser handling of these messages
        // send to editor handler instead for output
        
        // expecting !filter-add content reason
        // todo fix content to be able to be phrases
        if (trailing.startsWith("!filter-add")) {
            if (commandOptionsHandler.checkAuthorization("!filter-add", username, true, true)) {
                return (filterHandler.addFilter(trailing, username));
            }
        }
        // expecting !filter-delete content
        if (trailing.startsWith("!filter-delete")) {
            if (commandOptionsHandler.checkAuthorization("!filter-delete", username, true, true)) {
                return (filterHandler.deleteFilter(trailing, username));
            }
        }

        //expecting !regex-add [name] [content] [seconds] [reason] 
        // WITH [ ] 's in place
        if (trailing.startsWith("!regex-add")) {
            if (commandOptionsHandler.checkAuthorization("!regex-add", username, true, true)) {
                return (filterHandler.addRegex(trailing));
            }
        }

        //expecting !regex-edit [name] [attribute] [new value]
        if (trailing.startsWith("!regex-edit")) {
            if (commandOptionsHandler.checkAuthorization("!regex-edit", username, true, true)) {
                return (filterHandler.editRegex(trailing));
            }
        }

        //expecting !regex-del [name]
        if (trailing.startsWith("!regex-del")) {
            if (commandOptionsHandler.checkAuthorization("!regex-del", username, true, true)) {
                return (filterHandler.delRegex(trailing));
            }
        }

        return "";
    }
}
