/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.settings;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import com.twitchbotx.gui.guiHandler;
//import com.twitchbotx.bot.handlers.LotteryHandler;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
//import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
//import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ConfigurationController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;

    @FXML
    Label saveText;

    @FXML
    TextField testMessageText;

    @FXML
    TextField soundTestText;

    @FXML
    RadioButton spoopEnabled;

    @FXML
    RadioButton spoopDisabled;

    @FXML
    RadioButton marEnabled;

    @FXML
    RadioButton marDisabled;

    @FXML
    RadioButton songEnabled;

    @FXML
    RadioButton songDisabled;

    @FXML
    RadioButton lottoEnabled;

    @FXML
    RadioButton lottoDisabled;

    @FXML
    ToggleGroup Spoop;

    @FXML
    ToggleGroup Marathon;

    @FXML
    ToggleGroup songLotto;

    @FXML
    ToggleGroup lotto;
    
    @FXML
    ToggleGroup sub;

    @FXML
    AnchorPane background;

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
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
        try {
            guiHandler.bot.getOut().println("PRIVMSG #"
                    + guiHandler.bot.getStore().getConfiguration().joinedChannel
                    + " "
                    + ":"
                    + message);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    guiHandler.bot.getStore().getEventList().addList("Attempted to send: \'" + message + "\' to chat, reconnect if failed");
                }
            });
        } catch (Exception e) {
            System.out.println("error occured sending test");
            e.printStackTrace();
        }
    }

    // Change to sub editing page
    @FXML
    private void goResponseEditor(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.respEditID, guiHandler.respEditFile);
        myController.setScreen(guiHandler.respEditID);
        myController.setId("RespEdit");
        myController.show(myController);
    }
    
    
    
    @FXML
    public void saveSettings() {
        //grab radiobuttons, etc and save all content
        RadioButton toggle = (RadioButton) Spoop.getSelectedToggle();
        String choice = toggle.getText();
        System.out.println(choice);
        String enabled;
        if (choice.equals("Enabled")) {
            enabled = "on";
        } else {
            enabled = "off";
        }
        //send enabled to spoopathon here
        store.modifyConfiguration("sStatus", enabled);
        System.out.println("Spoopathon:" + enabled);

        toggle = (RadioButton) Marathon.getSelectedToggle();
        if (toggle.getText().equals("Enabled")) {
            enabled = "on";
        } else {
            enabled = "off";
        }
        //send enabled to marathon here
        store.modifyConfiguration("mStatus", enabled);
        System.out.println("marathon:" + enabled);

        toggle = (RadioButton) songLotto.getSelectedToggle();
        if (toggle.getText().equals("Enabled")) {
            enabled = "on";
            CommandParser.songs.songEnable();
        } else {
            enabled = "off";
            CommandParser.songs.songDisable();
        }
        //send enabled to songLotto here
        store.modifyConfiguration("songLottoStatus", enabled);
        System.out.println("songLotto:" + enabled);

        toggle = (RadioButton) lotto.getSelectedToggle();
        if (toggle.getText().equals("Enabled")) {
            enabled = "on";
            CommandParser.lotto.lottoEnable();
        } else {
            enabled = "off";
            CommandParser.lotto.lottoDisable();
        }
        //send enabled to lotto here
        store.modifyConfiguration("lottoStatus", enabled);
        System.out.println("lotto:" + enabled);

        // set value for sub reply system(this will only stop bot from sending to chat)
        toggle = (RadioButton) sub.getSelectedToggle();
        if (toggle.getText().equals("Enabled")) {
            enabled = "on";
        } else {
            enabled = "off";
        }
        store.modifyConfiguration("subReply", enabled);
        System.out.println("subReply:" + enabled);

        
        
        //print confirmation text
        saveText.setVisible(true);
    }

    int key = 0;
    KeyCode left = KeyCode.LEFT;
    KeyCode right = KeyCode.RIGHT;
    KeyCode up = KeyCode.UP;
    KeyCode down = KeyCode.DOWN;
    KeyCode lastKey = up;

    // begin rogue
    public void incrementKey() {
        key++;
    }

    @FXML
    public void keyPressed() {
        EventHandler<KeyEvent> keyPressed = (EventHandler<KeyEvent>) background.getOnKeyTyped();

    }

    public static class goRogue {

        public static void begin() {
            String message = "testing a thing";
            send(message);
        }

        private static void send(String msg) {
            try {
                guiHandler.bot.getOut().println("PRIVMSG #"
                        + guiHandler.bot.getStore().getConfiguration().joinedChannel
                        + " "
                        + ":"
                        + msg);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        guiHandler.bot.getStore().getEventList().addList("BOT WILL NO LONGER TAKE COMMANDS");
                    }
                });
            } catch (Exception e) {
                System.out.println("error occured sending test");
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        store = guiHandler.bot.getStore();
        key = 0;
        if (store.getConfiguration().lottoStatus.equals("on")) {
            lottoEnabled.setSelected(true);
        } else {
            lottoDisabled.setSelected(true);
        }
        if (store.getConfiguration().songLottoStatus.equals("on")) {
            songEnabled.setSelected(true);
        } else {
            songDisabled.setSelected(true);
        }
        if (store.getConfiguration().spoopathonStatus.equals("on")) {
            spoopEnabled.setSelected(true);
        } else {
            spoopDisabled.setSelected(true);
        }
        if (store.getConfiguration().marathonStatus.equals("on")) {
            marEnabled.setSelected(true);
        } else {
            marDisabled.setSelected(true);
        }
        saveText.setVisible(false);

        testMessageText.setOnKeyPressed((KeyEvent event) -> {
            lastKey = event.getCode();
            System.out.println("Pressed the " + lastKey.getName() + " key");
        });

    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
