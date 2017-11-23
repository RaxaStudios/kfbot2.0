/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ConfigurationController implements Initializable {

    ScreensController myController = new ScreensController();

    @FXML
    TextField testMessageText;

    @FXML
    TextField soundTestText;

    @FXML
    private void dash(ActionEvent event) {
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    // Takes user input filename and attempts to play
    // must be mp3 or wav
    
    @FXML
    private void playTestSound() {
        try {
            soundTestText.selectAll();
            soundTestText.copy();
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve(soundTestText.getText());
            Media hit = new Media(xmlResolved.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList("Successful sound test");
            }
        });
        } catch (Exception e) {
            soundTestText.setText("Error playing song");
        }
    }

    // Takes user input message to send to chat
    
    @FXML
    private void sendTestMessage() {
        testMessageText.selectAll();
        testMessageText.copy();
        String message = testMessageText.getText();
        guiHandler.bot.getOut().println("PRIVMSG #"
                + guiHandler.bot.getStore().getConfiguration().joinedChannel
                + " "
                + ":"
                + message);
                Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList("Successfully sent: \'" + message + "\' to chat");
            }
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
