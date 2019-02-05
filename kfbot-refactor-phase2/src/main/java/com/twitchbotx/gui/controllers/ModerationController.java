/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ModerationController implements Initializable {

    @FXML
    TextField filterText;

    @FXML
    TextField timeoutValue;

    @FXML
    TextArea reasonMsg;

    @FXML
    RadioButton enabledT;

    @FXML
    RadioButton enabledF;

    @FXML
    ToggleGroup enabled;

    @FXML
    Label filterStatus;

    @FXML
    ListView<String> filterList;

    ObservableList<String> filterObL = FXCollections.observableArrayList();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    ScreensController myController = new ScreensController();

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    @FXML
    private void deleteFilter(ActionEvent event) {
        String filterD = filterText.getText();
        if (store.deleteFilter(filterD)) {
            filterStatus.setText("Filter Deleted");
            filterObL.remove(filterObL.indexOf(filterD));
        } else if (filterD.equals("")) {
            filterStatus.setText("Select a filter");
        } else {
            filterStatus.setText("Filter does not exists");
        }
        filterStatus.setVisible(true);
        filterText.setText("");
        reasonMsg.clear();
        timeoutValue.clear();
    }

    @FXML
    private void submitFilter(ActionEvent event) {
        String filterName = filterText.getText();
        if (!filterName.replaceAll(" ", "").equals("")) {
            filterName = filterText.getText();
        } else {
            filterText.setText("Filter name empty");
            return;
        }
        String timeout = timeoutValue.getText();
        String reason = reasonMsg.getText();
        RadioButton toggle = (RadioButton) enabled.getSelectedToggle();
        boolean enabledToggle = Boolean.parseBoolean(toggle.getText());
        final ConfigParameters.Filter filter = new ConfigParameters.Filter();
        filter.enabled = enabledToggle;
        filter.name = filterName;
        filter.reason = reason;
        filter.seconds = timeout;
        if (store.addFilter(filter)) {
            filterStatus.setText("Filter added");
            filterObL.add(filterName);
            filterStatus.setVisible(true);
        } else {
            for (int i = 0; i < store.getFilters().size(); i++) {
                String filterGet = store.getFilters().get(i).name;
                if (filterGet.equals(filterName)) {
                    if (store.updateFilter(filter)) {
                        filterStatus.setText("Filter updated");
                        filterStatus.setVisible(true);
                    }
                }
            }
        }
        filterText.setText("");
        reasonMsg.clear();
        timeoutValue.clear();
    }

    @FXML
    private void selectFilter(ActionEvent event) {
        filterStatus.setVisible(false);
        int selected = filterList.getSelectionModel().getSelectedIndex();
        String fText = filterList.getItems().get(selected);
        filterText.setText(fText);
        for (int i = 0; i < store.getFilters().size(); i++) {
            String filter = store.getFilters().get(i).name;
            if (filter.equals(fText)) {
                String reason = store.getFilters().get(i).reason;
                boolean enabledToggle = store.getFilters().get(i).enabled;
                String timeout = store.getFilters().get(i).seconds;
                if (enabledToggle) {
                    enabled.selectToggle(enabledT);
                } else {
                    enabled.selectToggle(enabledF);
                }
                timeoutValue.setText(timeout);
                reasonMsg.setText(reason);
            }
        }

    }

    @FXML
    public void regex(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.regexID, guiHandler.regexFile);
        myController.setScreen(guiHandler.regexID);
        myController.setId("regex");
        myController.show(myController);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        filterStatus.setVisible(false);
        store = guiHandler.bot.getStore();
        String[] filters = new String[this.store.getFilters().size()];
        for (int i = 0; i < store.getFilters().size(); i++) {
            final ConfigParameters.Filter filter = store.getFilters().get(i);
            filters[i] = filter.name;
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
