/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class RegexController implements Initializable {

    @FXML
    ListView<String> filterList;

    @FXML
    TextField editIndex;
    @FXML
    TextField deleteIndex;
    @FXML
    TextField newRegex;

    ObservableList<String> filterObL = FXCollections.observableArrayList();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    ScreensController myController = new ScreensController();

    /*
    *  TODO REGEX options
     */
    @FXML
    public void addRegex(ActionEvent event) {

    }

    @FXML
    public void delRegex(ActionEvent event) {

    }

    @FXML
    public void editRegex(ActionEvent event) {

    }

    @FXML
    private void moderation(ActionEvent event) {
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
        // TODO
        store = guiHandler.bot.getStore();
                String[] filters = new String[this.store.getRegexes().size()];
        for (int i = 0; i < store.getRegexes().size(); i++) {
            final ConfigParameters.FilterRegex filter = store.getRegexes().get(i);
            filters[i] = (i+1) + " " + filter.content;
        }
        for (int j = 0; j < filters.length; j++) {
            String filter = filters[j];
            filterObL.add(filter);
        }
        filterList.setItems(filterObL);
    }

}
