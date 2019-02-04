/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.apache.commons.lang.ArrayUtils;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ConfigurationController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;
    String[] code = {"Up", "Up", "Down", "Down", "Left", "Right", "Left", "Right", "B", "A"};
    String[] input = new String[10];
    int codeInt;

    @FXML
    Label saveText;

    @FXML
    TextField testMessageText;

    @FXML
    TextField nonFormatMessage;
    
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
            DashboardController.wIRC.sendMessage(message, false);
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
    
    
    @FXML
    private void sendNonFormatMessage() {
        nonFormatMessage.selectAll();
        nonFormatMessage.copy();
        String message = nonFormatMessage.getText();
        System.out.println("Attempted to send: " + message);
        try {
            DashboardController.wIRC.sendNonFormatMessage(message);
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

    public void konami(String pressed) {
        if (ArrayUtils.contains(code, pressed)) {
            if (code[codeInt].equals(pressed)) {
                input[codeInt] = pressed;
                codeInt++;
                System.out.println(codeInt + " " + Arrays.asList(input));
                if (codeInt == 10) {
                    System.out.println("10 codeInt reached");
                    codeInt = 0;
                    if (Arrays.equals(code, input)) {
                        System.out.println("Code success");
                        try {
                            URL wav = new URL("https://kf.bot.raxastudios.com/Tetris.wav");
                            AudioClip audioClip = new AudioClip(wav.toExternalForm());
                            audioClip.play();
                            Image t = new Image("https://kf.bot.raxastudios.com/tetrisHeart.png");
                            ImageView iT = new ImageView(t);
                            Group root = new Group();
                            root.getChildren().add(iT);
                            Stage show = new Stage();
                            Scene tetris = new Scene(root);
                            show.setScene(tetris);
                            show.show();
                            show.setOnCloseRequest(e -> audioClip.stop());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Arrays.fill(input, null);
                }
            } else {
                //incorrect
                System.out.println("Incorrect input");
                Arrays.fill(input, null);
                codeInt = 0;
            }
        } else {
            codeInt = 0;
            Arrays.fill(input, null);
        }

    }

    public static class goRogue {

        static boolean end = false;
        static ArrayList<String> commands = new ArrayList<>();
        static int c = 0;
        public static void begin() {
            try {
                c = 0;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        guiHandler.bot.getStore().getEventList().addList("BOT IS OVER IT");
                    }
                });
                URL text = new URL("https://kf.bot.raxastudios.com/rogue.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(text.openStream()));
                ArrayList<String> list = new ArrayList<>();
                String line;

                while ((line = in.readLine()) != null) {
                    list.add(line);
                }
                in.close();
                System.out.println(list);

                ScheduledExecutorService ses = Executors.newScheduledThreadPool(10);
                ses.scheduleAtFixedRate(new Runnable() {
                    int i = 0;

                    @Override
                    public void run() {
                        if (!guiHandler.bot.rogue) {
                            ses.shutdown();
                        } else {
                            if ((i > list.size() - 1) || end) {
                                System.out.println("overflow");
                                ses.shutdown();
                            } else {
                                String message = getLine(list);
                                if (!message.equals("")) {
                                    send(message);
                                    i++;
                                } else {
                                    System.out.println("shutting down");
                                    ses.shutdownNow();
                                }
                            }
                        }
                    }

                    private String getLine(ArrayList<String> list) {
                        int size = list.size();
                        String send = "";
                        Random rng = new Random();
                        if (size > 0) {
                            //TODO radomize get
                            //send = list.get(rng.nextInt(list.size()));
                            send = list.get(i);
                        }
                        return send;
                    }

                    private void send(String msg) {
                        try {
                           DashboardController.wIRC.sendMessage(msg, true);
                        } catch (Exception e) {
                            System.out.println("error occured sending test");
                            e.printStackTrace();
                        }
                    }
                }, 0, 7, TimeUnit.MINUTES);
                
                URL cmd = new URL("https://kf.bot.raxastudios.com/rogueCommands.txt");
                BufferedReader inCMD = new BufferedReader(new InputStreamReader(cmd.openStream()));
                String cLine;
                while ((cLine = in.readLine()) != null) {
                    commands.add(cLine);
                }
                inCMD.close();
                System.out.println(cLine);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void end() {
            end = true;
        }

        public static void sassCommand(boolean mod) {
            if(c < commands.size()-1){
                send("Okay fine, I give up, try that command again if you must...");
                guiHandler.bot.rogue = false;
                c = 0;
            } else {
            send(commands.get(c));
            c++;
            }
        }

        private static void send(String msg) {
            try {
                DashboardController.wIRC.sendMessage(msg, true);
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

        Arrays.fill(input, null);
        codeInt = 0;
        testMessageText.setOnKeyPressed((KeyEvent event) -> {
            lastKey = event.getCode();
            System.out.println("Pressed the " + lastKey.getName() + " key");
            konami(lastKey.getName());
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
