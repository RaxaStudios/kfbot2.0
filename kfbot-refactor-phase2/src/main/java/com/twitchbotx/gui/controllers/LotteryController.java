/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.handlers.*;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class LotteryController implements Initializable {

    ScreensController myController = new ScreensController();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    LinkedHashMap<String, LotteryHandler.Entrant<Integer, String>> LOTTOMAP;
    public static LotteryHandler.Lotto lotto = CommandParser.lotto;
    static int labelLotto = 0;

    String winner = "";

    @FXML
    private TextField addUserTextField;

    @FXML
    private TextField keywordText;

    @FXML
    private RadioButton subRadioButton;

    @FXML
    ListView userLottoListView;
    public static ObservableList<String> userLottoList = FXCollections.observableArrayList();

    @FXML
    ScrollPane queueList;

    @FXML
    Pane winnerPane;

    @FXML
    Label winnerText;

    @FXML
    Label qStatus;

    @FXML
    MenuItem song;

    @FXML
    MenuItem regular;

    @FXML
    ToggleGroup songLottoGroup;

    @FXML
    ToggleGroup lottoGroup;

    @FXML
    RadioButton songEnabled;

    @FXML
    RadioButton songDisabled;

    @FXML
    RadioButton lottoEnabled;

    @FXML
    RadioButton lottoDisabled;

    @FXML
    public void showQueue() {
        userLottoList.clear();
        LOTTOMAP = lotto.getMap();
        //uncomment for debugging
        /*LOTTOMAP.entrySet().forEach((m) -> {
        //System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
        });*/
        LOTTOMAP.entrySet().forEach((m) -> {
            userLottoList.add(" " + (labelLotto + 1) + ". " + m.getKey());
            labelLotto++;
        });
        labelLotto = 0;
        userLottoListView.setItems(userLottoList);

    }

    @FXML
    public void clearQueue() {
        lotto.lottoClear();
        showQueue();
        qStatus.setText("Lottery queue cleared");
    }

    @FXML
    public void openQueue() {
        //Handle text from keyword box
        //Check sub only button
        //Clear and enable list
        keywordText.selectAll();
        keywordText.copy();
        System.out.println("Keyword set to: " + keywordText.getText());
        String sub = "+a";
        if (!keywordText.getText().isEmpty()) {
            qStatus.setAlignment(Pos.CENTER);
            if (subRadioButton.isSelected()) {
                sub = "+s";
                qStatus.setText("A sub-only lottery has started! Subs can type " + keywordText.getText() + " to enter!");
            } else {
                qStatus.setText("Lottery has started! Type " + keywordText.getText() + " to enter!");
            }
            addUserTextField.clear();
            winnerText.setText("");
            lotto.lottoOpen("!lotto-open " + sub + " " + keywordText.getText());
        } else {
            qStatus.setText("No keyword found to open lottery");
        }

    }

    @FXML
    public void sendLottoReminder() {
        if (subRadioButton.isSelected()) {
            sendMessage("A sub-only lottery is running! Subs can type " + keywordText.getText() + " to enter!");
        } else {
            sendMessage("A lottery is running! Type " + keywordText.getText() + " to enter!");
        }
    }

    @FXML
    public void closeQueue() {
        lotto.lottoClose();
        addUserTextField.setText("Lottery closed");
        // send message to user to join bot to channel before using lottery
        qStatus.setText("Lottery currently closed");
    }

    @FXML
    public void addUser() {
        if (lotto.getLottoStatus()) {
            addUserTextField.selectAll();
            addUserTextField.copy();

            if (!addUserTextField.getText().isEmpty()) {
                try {
                    if (lotto.getCurr().contains(addUserTextField.getText()) || addUserTextField.getText().equals("Duplicate entry found")) {
                        addUserTextField.setText("Duplicate entry found");
                    } else {
                        lotto.addUser(addUserTextField.getText(), true);
                        showQueue();
                    }
                } catch (NullPointerException n) {
                    addUserTextField.setText("Lottery is closed");
                }
            } else {
                addUserTextField.setText("Empty add field");
            }
        } else {
            addUserTextField.setText("Lottery is closed");
        }
    }

    @FXML
    public void popUser() {
        try {
            boolean isEmpty = lotto.getCurr().isEmpty();
            if (!isEmpty) {
                winner = lotto.drawLotto();
                System.out.println("winner set to " + winner);
                if (lotto.getCurr().isEmpty()) {
                    winnerText.setAlignment(Pos.CENTER);
                    sendMessage("Lottery is empty");
                    winnerText.setText("Lottery is empty");
                    showQueue();
                    
                } else if(!winner.equals("empty")) {
                    winnerText.setAlignment(Pos.CENTER);
                    winnerText.setText(winner);
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    sendEvent(sdf.format(cal.getTime()) + " lottery winner: " + winner);
                    showQueue();
                }
            } else {
                winnerText.setAlignment(Pos.CENTER);
                sendMessage("Lottery is empty");
                winnerText.setText("Lottery is empty");
                showQueue();
            }
        } catch (NullPointerException n) {
            sendMessage("Lottery is closed");
            winnerText.setText("Lottery is closed");
        }
    }

    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(event);
            }
        });
    }

    @FXML
    public void goToDashboard() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    // begin song lottery tab
    LinkedHashMap<String, LotteryHandler.Entrant<Integer, String>> SONGMAP;
    public static LotteryHandler.SongList entries = CommandParser.songs;
    static int labelSong = 0;

    @FXML
    ListView userSongListView;
    public static ObservableList<String> userSongList = FXCollections.observableArrayList();

    @FXML
    Label songStatusWindow;

    @FXML
    TextField usernameSong;

    @FXML
    TextField songName;

    @FXML
    Label songWinnerName;

    @FXML
    Label winnerSong;

    @FXML
    ScrollPane songQueueList;

    @FXML
    public void openSongLotto() {
        entries.songOpen();
        songStatusWindow.setText("Lottery has been opened !song [song number] to enter");
    }

    @FXML
    public void sendSongReminder() {
        sendMessage("A song lottery is running! Type !song [song number] (without the brackets) to enter");
    }

    @FXML
    public void closeSongLotto() {
        entries.songClose();
        songStatusWindow.setText("Lottery has been closed");
    }

    @FXML
    public void addSongUser() {
        usernameSong.selectAll();
        usernameSong.copy();
        String user = usernameSong.getText();

        songName.selectAll();
        songName.copy();
        String content = songName.getText();
        if (user.equals("") || content.equals("")) {
            songStatusWindow.setText("Username and song name required");
        } else {
            if (entries.addUser(user, "!song " + content)) {
                songStatusWindow.setText(user + " has been added with song: " + content);
            } else {
                songStatusWindow.setText("Song:" + content + " or user: " + user + " already in queue or already played today");
            }
            usernameSong.setText("");
            songName.setText("");
            showSongQueue();
        }
    }

    @FXML
    public void drawSongWinner() {
        String drawnWinner = entries.drawSong();
        if (drawnWinner.equals("notPresent")) {
            drawnWinner = entries.drawSong();
        }
        ///System.out.println(winner);
        int begIndex = drawnWinner.indexOf("song:") + 6;
        int endIndex = drawnWinner.length();
        try {
            //TODO song conversion from number to song title
            String winningSong = drawnWinner.substring(begIndex, endIndex);
            drawnWinner = drawnWinner.substring(0, drawnWinner.indexOf("song:") - 1);
            //System.out.println("JD draw print: " + drawnWinner + " " + winningSong);
            songWinnerName.setText(drawnWinner);
            winnerSong.setText(winningSong);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sendEvent(sdf.format(cal.getTime()) + " song lottery winner: " + winner + " with song: " + song);
            showSongQueue();
        } catch (StringIndexOutOfBoundsException se) {
            songStatusWindow.setText("Lotto is empty");
        }
    }

    @FXML
    public void clearSongQueue() {
        entries.songReset();
        songStatusWindow.setText("Lottery has been emptied and reset");
        showSongQueue();
    }

    @FXML
    public void showSongQueue() {
        userSongList.clear();
        SONGMAP = entries.getMap();
        //uncomment for debugging
        /*SONGMAP.entrySet().forEach((m) -> {
            //System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
        });*/
        SONGMAP.entrySet().forEach((m) -> {
            userSongList.add(" " + (labelSong + 1) + ". " + m.getKey() + ": " + m.getValue().getContent());
            labelSong++;
        });
        labelSong = 0;
        userSongListView.setItems(userSongList);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            LOTTOMAP = lotto.getMap();
            SONGMAP = entries.getMap();
            store = guiHandler.bot.getStore();
            qStatus.setText("Lottery ready to use");
            songStatusWindow.setText("Song Lotto ready");
            showQueue();
            showSongQueue();
            setRadios();
            addListener(lottoGroup, "lottoStatus");
            addListener(songLottoGroup, "songLottoStatus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRadios() {
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
    }

    /**
     * adds a listener to the radio button toggle group instant change in on/off
     * status for group
     *
     * @param ToggleGroup to listen to
     * @param String config name associated with XML value
     */
    private void addListener(ToggleGroup group, String name) {
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ob,
                    Toggle o, Toggle n) {

                RadioButton rb = (RadioButton) group.getSelectedToggle();

                if (rb != null) {
                    String s = rb.getText();
                    String enabled;
                    if (s.equals("Enabled")) {
                        enabled = "on";
                    } else {
                        enabled = "off";
                    }
                    store.modifyConfiguration(name, enabled);
                    System.out.println(name + ":" + enabled);
                }
            }

        });
    }

    private void sendMessage(String msg) {
        try {
            DashboardController.wIRC.sendMessage(msg, true);
        } catch (NullPointerException ne) {
            // send message to user to join bot to channel before using lottery
            qStatus.setText("Bot must connect first");
        }
    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
