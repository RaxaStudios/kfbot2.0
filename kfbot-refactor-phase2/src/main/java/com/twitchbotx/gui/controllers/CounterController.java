/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.handlers.sqlHandler;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class CounterController implements Initializable {

    Datastore store = guiHandler.bot.getStore();
    int value = 0;
    String text = "";
    ScreensController myController = new ScreensController();
    private sqlHandler countData = new sqlHandler(store);
    
    
    @FXML
    Label countLabel;
    
    @FXML
    TextArea textField;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        textField.setText(store.getConfiguration().countText);
        countLabel.setText(store.getConfiguration().currentCount);
    }    
    
    @FXML
    public void submitText(){
        try{
        textField.selectAll();
        textField.copy();
        text = textField.getText();
        System.out.println("attempt to set text:" + text);
        store.modifyConfiguration("countText", text);
        if(countData.setCountText(text)){
            System.out.println("set text");
        }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @FXML
    public void increase(){
        try{
        value = Integer.parseInt(store.getConfiguration().currentCount);
        value++;
        store.modifyConfiguration("currentCount", String.valueOf(value));
        countLabel.setText(String.valueOf(value));
        countData.setCountValue(value);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @FXML
    public void decrease(){
        value = Integer.parseInt(store.getConfiguration().currentCount);
        value--;
        store.modifyConfiguration("currentCount", String.valueOf(value));
        countLabel.setText(String.valueOf(value));
        countData.setCountValue(value);
    }
    
    @FXML
    public void reset(){
        store.modifyConfiguration("currentCount", String.valueOf(0));
        countLabel.setText(String.valueOf(0));
        countData.setCountValue(0);
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
