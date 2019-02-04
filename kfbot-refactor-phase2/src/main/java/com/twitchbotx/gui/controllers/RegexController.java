/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class RegexController implements Initializable {

    String[] filters;

    @FXML
    ListView<String> filterList;

    ObservableList<String> filterOBL = FXCollections.observableArrayList();

    @FXML
    TextField editIndex;

    @FXML
    TextField deleteIndex;

    @FXML
    TextArea regexText;

    @FXML
    TextField regexName;

    @FXML
    TextField regexReason;

    @FXML
    TextField regexSeconds;

    @FXML
    RadioButton enable;

    @FXML
    RadioButton disable;

    @FXML
    ToggleGroup enabled;

    ObservableList<String> filterObL = FXCollections.observableArrayList();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    ScreensController myController = new ScreensController();

    @FXML
    private void select() {
        int selected = filterList.getSelectionModel().getSelectedIndex();
        String f = filterList.getItems().get(selected).substring(2).replaceAll(" ", "");
        String filterName = "";
        String filterContent = "";
        String filterSeconds = "";
        String filterReason = "";
        boolean filterEnabled = false;
        for (int i = 0; i < store.getRegexes().size(); i++) {
            final ConfigParameters.FilterRegex filter = store.getRegexes().get(i);
            if (filter.name.equals(f)) {
                filterName = filter.name;
                filterContent = filter.content;
                filterSeconds = filter.seconds;
                filterEnabled = filter.enabled;
                filterReason = filter.reason;
            }
        }
        regexName.setText(filterName);
        deleteIndex.setText(filterName);
        editIndex.setText(filterName);
        regexText.setText(filterContent);
        regexSeconds.setText(filterSeconds);
        regexReason.setText(filterReason);
        if (filterEnabled) {
            enabled.selectToggle(enable);
        } else {
            enabled.selectToggle(disable);
        }
    }

    @FXML
    public void addRegex(ActionEvent event) {
        String name;
        regexName.selectAll();
        regexName.copy();
        name = regexName.getText();
        String content;
        regexText.selectAll();
        regexText.copy();
        content = regexText.getText();
        String seconds;
        regexSeconds.selectAll();
        regexSeconds.copy();
        seconds = regexSeconds.getText();
        String reason;
        regexReason.selectAll();
        regexReason.copy();
        reason = regexReason.getText();
        //expecting !regex-add [name] [content] [seconds] [reason]
        String msg = "!regex-add [" + name + "] [" + content + "] [" + seconds + "] [" + reason + "]";
        CommandParser.filterHandler.addRegex(msg);
    }

    @FXML
    public void delRegex(ActionEvent event) {
        deleteIndex.selectAll();
        deleteIndex.copy();
        String name = deleteIndex.getText();
        //expecting !regex-del [name]
        String msg = "!regex-del [" + name + "]";
        CommandParser.filterHandler.delRegex(msg);
    }

    @FXML
    public void editRegex(ActionEvent event) {
        boolean enabledValue = true;
        String name;
        regexName.selectAll();
        regexName.copy();
        name = regexName.getText();
        String content;
        regexText.selectAll();
        regexText.copy();
        content = regexText.getText();
        String seconds;
        regexSeconds.selectAll();
        regexSeconds.copy();
        seconds = regexSeconds.getText();
        String reason;
        regexReason.selectAll();
        regexReason.copy();
        reason = regexReason.getText();
        //send to store as object to avoid excess checking
        ConfigParameters.FilterRegex regex = new ConfigParameters.FilterRegex();
        regex.content = content;
        regex.name = name;
        regex.reason = reason;
        regex.seconds = seconds;
        regex.enabled = enabledValue;
        store.updateRegex(regex, ""); // send in "" as attribute to enable writing of all values
    }

    @FXML
    private void enableRegex() {
        RadioButton toggle = (RadioButton) enabled.getSelectedToggle();
        String eRegex = toggle.getText();
        if (eRegex.equals("On")) {
            eRegex = "true";
        } else {
            eRegex = "false";
        }
        editIndex.selectAll();
        editIndex.copy();
        String name = editIndex.getText();
        String msg = "!regex-edit [" + name + "] [enabled] [" + eRegex + "]";
        //expecting !regex-edit [name] [attribute] [new value]
        CommandParser.filterHandler.editRegex(msg);
    }

    @FXML
    private void moderation(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.moderationID, guiHandler.moderationFile);
        myController.setScreen(guiHandler.moderationID);
        myController.setId("moderation");
        myController.show(myController);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        store = guiHandler.bot.getStore();
        filters = new String[this.store.getRegexes().size()];
        for (int i = 0; i < store.getRegexes().size(); i++) {
            final ConfigParameters.FilterRegex filter = store.getRegexes().get(i);
            filters[i] = (i + 1) + " " + filter.name;
        }
        for (int j = 0; j < filters.length; j++) {
            String filter = filters[j];
            filterObL.add(filter);
        }
        filterList.setItems(filterObL);
    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
