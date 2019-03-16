/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import com.twitchbotx.bot.WSClass;
import com.twitchbotx.bot.XmlDatastore;
import com.twitchbotx.bot.handlers.CommonUtility;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class DashboardController implements Initializable{

    ScreensController myController = new ScreensController();
    guiHandler.dimensions dm = ScreensController.dm;
    TwitchBotX bot;
    Datastore store;

    public static WSClass wIRC;

    static int visitCount = 0;

    public static XmlDatastore.eventObList eventObL = new XmlDatastore.eventObList();

    @FXML
    private ListView<String> eventList;

    @FXML
    private Button commands;

    @FXML
    private Button configuration;

    @FXML
    private Button moderation;

    @FXML
    private Button lottery;

    @FXML
    private Button spoopathon;

    @FXML
    private Button marathon;

    private static JFXPanel dashContainer;
    private static final int JFXPANEL_WIDTH_INT = 600;
    private static final int JFXPANEL_HEIGHT_INT = 400;

    @FXML
    private void commands(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.commandsID, guiHandler.commandsFile);
        myController.setScreen(guiHandler.commandsID);
        myController.setId("commands");
        myController.show(myController);
    }

    @FXML
    private void configuration(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.configurationID, guiHandler.configurationFile);
        myController.setScreen(guiHandler.configurationID);
        myController.setId("configuration");
        myController.show(myController);
    }

    @FXML
    private void moderation(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.moderationID, guiHandler.moderationFile);
        myController.setScreen(guiHandler.moderationID);
        myController.setId("moderation");
        myController.show(myController);
    }

    @FXML
    private void features(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.featuresID, guiHandler.featuresFile);
        myController.setScreen(guiHandler.featuresID);
        myController.setId("features");
        myController.show(myController);
    }

    @FXML
    private void startBot(ActionEvent event) {
        Thread botT;
        try {
            eventObL.addList("Starting bot");
            botT = new Thread() {
                @Override
                public void run() {
                    //open websocket connection
                    try {
                        wIRC = new WSClass(
                                new URI("wss://irc-ws.chat.twitch.tv"),
                                store.getConfiguration().joinedChannel,
                                store.getConfiguration().account,
                                store.getConfiguration().password,
                                store);

                        if (!wIRC.connectWSS(false)) {
                            throw new Exception("Error when connecting to Twitch.");
                        } else {
                            guiHandler.bot.start(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            botT.start();
            eventObL.addList("Bot connected to chat");
        } catch (NullPointerException e) {
            CommonUtility.ERRORLOGGER.severe(e.toString());
            eventObL.addList("No instance to cancel, restart the application");
        } catch (Exception e) {
            CommonUtility.ERRORLOGGER.severe(e.toString());
            e.printStackTrace();
            eventObL.addList("General error occured creating the bot, restart the application");
        }
    }

    @FXML
    private void restartBot(ActionEvent event) {
      wIRC.close();
    }

    public Scene getScene() throws IOException {
        Parent dash = FXMLLoader.load(getClass().getResource("./Dashboard.fxml"));
        Scene dashBoard = new Scene(dash);
        return dashBoard;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        store = guiHandler.bot.getStore();
        store.setLV(eventList);
        store.getLV().setItems(eventObL.getList());
        store.setEvent(eventObL);
        if (visitCount == 0) {
            eventObL.addList("Bot ready to connect");
        }
        visitCount++;
    }

    /* public void eventObLAdd(String msg) {
        eventObL.addList(msg);
        eventList.setItems(eventObL.getList());
        eventList.scrollTo(eventObL.getList().size() - 1);
    }*/


    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
