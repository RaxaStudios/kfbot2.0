/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import eu.mihosoft.scaledfx.ScalableContentPane;
import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Raxa
 */
public class ScreensController extends StackPane {

    private HashMap<String, Node> screens = new HashMap<>();

    public ScreensController() {
        super();
    }

    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }

    public Node getScreen(String name) {
        return screens.get(name);
    }

    public boolean loadScreen(String name, String resource) {
        AnchorPane r = null;
        try {
            FXMLLoader loadFXML = new FXMLLoader();
            loadFXML.setLocation(getClass().getClassLoader().getResource(resource));
            Parent content = loadFXML.load();
            r = (AnchorPane) content;
            addScreen(name, r);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //This method tries to displayed the screen with a predefined name.
    //First it makes sure the screen has been already loaded.  Then if there is more than
    //one screen the new screen is been added second, and then the current screen is removed.
    // If there isn't any screen being displayed, the new screen is just added to the root.
    public boolean setScreen(final String name) {
        if (screens.get(name) != null) {   //screen loaded

            if (!getChildren().isEmpty()) {    //if there is more than one screen

                getChildren().remove(0);                    //remove the displayed screen
                getChildren().add(0, screens.get(name));     //add the screen
            } else {
                setOpacity(1.0);
                getChildren().add(screens.get(name));       //no one else been displayed, then just show
            }
            return true;
        } else {
            System.out.println("screen hasn't been loaded!!! \n");
            return false;
        }
    }

    public void show(Node node) {
        Group group = new Group();
        node.setStyle("-fx-font-family: \"Comfortaa\";");
        group.getChildren().addAll(node);
        /*Scene visibleScreen = new Scene(group);
        visibleScreen.getStylesheets().add("https://fonts.googleapis.com/css?family=Comfortaa");
        guiHandler.stage.setScene(visibleScreen);*/
        
        //scalable content update
        //TODO set scale value based on last viewed page?
        ScalableContentPane scale = new ScalableContentPane();
        scale.setContent(group);
        Scene testScale = new Scene(scale, 600, 400);
        testScale.getStylesheets().add("https://fonts.googleapis.com/css?family=Comfortaa:700");
        guiHandler.stage.setScene(testScale);

    }

    //This method will remove the screen with the given name from the collection of screens
    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Screen didn't exist");
            return false;
        } else {
            return true;
        }
    }
}
