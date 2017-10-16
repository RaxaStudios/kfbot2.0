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
import java.awt.Label;
import java.awt.TextField;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class DashboardController implements Initializable, ControlledScreen {

    ScreensController myController = new ScreensController();

    TwitchBotX bot = guiHandler.bot;
    Datastore store = guiHandler.store;
    PrintStream out = guiHandler.out;

    @FXML
    private Label label;

    @FXML
    private Label invalid_label;

    @FXML
    private TextField username_box;

    @FXML
    private TextField password_box;

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
    private Thread botT;

    @FXML
    private void close(ActionEvent event) throws IOException {
        System.out.println("Closing bot");
        bot.cancel(); //stop running bot
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
        botT = new Thread() {
            @Override
            public void run() {
                try {
                    bot.cancel();
                } catch (NullPointerException e) {
                    System.out.println("No instance to cancel");
                }
                bot.createBot();
                bot.start();
                botT.setName("bot");
            }
        };
        botT.start();
    }

    @FXML
    private void restartBot(ActionEvent event) {

        botT = new Thread() {
            @Override
            public void run() {
                try {
                    bot.cancel();
                } catch (NullPointerException e) {
                    System.out.println("No instance to cancel");
                }
                bot.createBot();
                bot.start();
                botT.setName("bot");
            }
        };
        botT.start();
    }

    public Scene getScene() throws IOException {
        Parent dash = FXMLLoader.load(getClass().getResource("./Dashboard.fxml"));
        Scene dashBoard = new Scene(dash);
        return dashBoard;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setScreenParent(ScreensController screenParent) {
        myController = screenParent;
    }
}
