/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class FeatureLandingController implements Initializable {

    ScreensController myController = new ScreensController();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    
    @FXML
    private void lottery(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.lotteryID, guiHandler.lotteryFile);
        myController.setScreen(guiHandler.lotteryID);
        myController.setId("lottery");
        myController.show(myController);
    }

    // Change to sub editing page
    @FXML
    private void alerts(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.respEditID, guiHandler.respEditFile);
        myController.setScreen(guiHandler.respEditID);
        myController.setId("RespEdit");
        myController.show(myController);
    }
    
   @FXML
    private void events(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.eventEditID, guiHandler.eventEditFile);
        myController.setScreen(guiHandler.eventEditID);
        myController.setId("EventEdit");
        myController.show(myController);
    }
    
    @FXML
    private void marathon(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.marathonID, guiHandler.marathonFile);
        myController.setScreen(guiHandler.marathonID);
        myController.setId("EventEdit");
        myController.show(myController);
    }
    
    @FXML
    private void spoopathon(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.spoopathonID, guiHandler.spoopathonFile);
        myController.setScreen(guiHandler.spoopathonID);
        myController.setId("spoopathon");
        myController.show(myController);
    }
    
    @FXML
    private void tipFeature(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.tipFeatureID, guiHandler.tipFeatureFile);
        myController.setScreen(guiHandler.tipFeatureID);
        myController.setId("TipFeature");
        myController.show(myController);
    }
    
    @FXML
    private void poll(){
        setDimensions();
        myController.loadScreen(guiHandler.pollID, guiHandler.pollFile);
        myController.setScreen(guiHandler.pollID);
        myController.setId("Poll");
        myController.show(myController);
    }
    
    @FXML
    private void onScreen(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.screenEditID, guiHandler.screenEditFile);
        myController.setScreen(guiHandler.screenEditID);
        myController.setId("ScreenEdit");
        myController.show(myController);
    }
    
    @FXML
    private void countThing(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.counterID, guiHandler.counterFile);
        myController.setScreen(guiHandler.counterID);
        myController.setId("Counter");
        myController.show(myController);
    }
    
    @FXML
    public void goDash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }
      
    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }
}
