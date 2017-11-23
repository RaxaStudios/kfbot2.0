/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import com.twitchbotx.bot.XmlDatastore;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class DashboardController implements Initializable, ControlledScreen {

    ScreensController myController = new ScreensController();

    TwitchBotX bot;
    Datastore store;

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
    private void close(ActionEvent event) throws IOException {
        guiHandler.bot.cancel(); //stop running bot
        eventObL.addList("Bot has left channel");
    }

    @FXML
    private void commands(ActionEvent event) {
        myController.loadScreen(guiHandler.commandsID, guiHandler.commandsFile);
        myController.setScreen(guiHandler.commandsID);
        myController.setId("commands");
        myController.show(myController);
    }

    @FXML
    private void configuration(ActionEvent event) {
        myController.loadScreen(guiHandler.configurationID, guiHandler.configurationFile);
        myController.setScreen(guiHandler.configurationID);
        myController.setId("configuration");
        myController.show(myController);
    }

    @FXML
    private void moderation(ActionEvent event) {
        myController.loadScreen(guiHandler.moderationID, guiHandler.moderationFile);
        myController.setScreen(guiHandler.moderationID);
        myController.setId("moderation");
        myController.show(myController);
    }

    @FXML
    private void lottery(ActionEvent event) {
        myController.loadScreen(guiHandler.lotteryID, guiHandler.lotteryFile);
        myController.setScreen(guiHandler.lotteryID);
        myController.setId("lottery");
        myController.show(myController);
    }

    @FXML
    private void spoopathon(ActionEvent event) {

        myController.loadScreen(guiHandler.spoopathonID, guiHandler.spoopathonFile);
        myController.setScreen(guiHandler.spoopathonID);
        myController.setId("spoopathon");
        myController.show(myController);
    }

    @FXML
    private void marathon(ActionEvent event) {
        myController.loadScreen(guiHandler.marathonID, guiHandler.marathonFile);
        myController.setScreen(guiHandler.marathonID);
        myController.setId("marathon");
        myController.show(myController);
    }

    
    @FXML
    private void startBot(ActionEvent event) {
        Thread botT;
        try {
            eventObL.addList("Starting bot");
            try {
                store.getBot().cancel();
            } catch (NullPointerException ne) {
                //ignore
            }
            store.getBot().createBot();

            store.getBot().start(false);
            botT = new Thread() {
                @Override
                public void run() {
                    store.getBot().beginReadingMessages();
                }
            };
            botT.start();
            eventObL.addList("Bot connected to chat");
        } catch (NullPointerException e) {
            eventObL.addList("No instance to cancel, restart the application");
        } catch (Exception e) {
            e.printStackTrace();
            eventObL.addList("General error occured creating the bot, restart the application");
        }
    }

    @FXML
    private void restartBot(ActionEvent event) {
        Thread botT;
        try {
            eventObL.addList("Restarting bot");
            store.getBot().cancel();
            store.getBot().createBot();
            store.getBot().start(true);
            botT = new Thread() {
                @Override
                public void run() {
                    store.getBot().beginReadingMessages();
                }
            };
            botT.start();

            eventObL.addList("Bot connected to chat");
        } catch (NullPointerException e) {
            eventObL.addList("No instance to cancel, restart the application");
        } catch (Exception e) {
            e.printStackTrace();
            eventObL.addList("General error occured creating the bot, restart the application");
        }
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

    public void eventObLAdd(String msg) {
        eventObL.addList(msg);
        eventList.setItems(eventObL.getList());
        eventList.scrollTo(eventObL.getList().size() - 1);
    }

    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
}
