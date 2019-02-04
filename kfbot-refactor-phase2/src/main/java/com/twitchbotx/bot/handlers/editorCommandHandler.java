/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

// TODO permission levels 
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

    public static String parseForCommand(String user, int level, String msg, String channel) {

        return "";
    }

}
