/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.bot.client.TwitchMessenger;
import eu.mihosoft.scaledfx.ScalableContentPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.concurrent.Task;

/**
 *
 * @author Raxa
 */
public class guiHandler extends Application {

    //TODO
    public Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    public static TwitchMessenger messenger;
    public static PrintStream out;
    public BufferedReader in;
    public static TwitchBotX bot;
    public Socket socket;

    //refine event viewer mod actions, command updates, etc
    public static String dashboardID = "dashboard";
    public static String dashboardFile = "Dashboard.fxml";
    //public InputStream dashIn = this.getClass().getClassLoader().getResourceAsStream(dashboardFile);
    //add/edit/delete/cooldown/auth etc view all scrollable
    public static String commandsID = "commands";
    public static String commandsFile = "Commands.fxml";
    public static String commandEditorID = "commandEditor";
    public static String commandEditorFile = "CommandEditor.fxml";
    //configuration -> account settings, joined channel
    public static String configurationID = "configuration";
    public static String configurationFile = "Configuration.fxml";
    //moderation -> filters, phrases, toggles, spam
    public static String moderationID = "moderation";
    public static String moderationFile = "Moderation.fxml";
    public static String regexID = "regex";
    public static String regexFile = "Regex.fxml";
    //queue, lottery, raffle display users + order, scrollable
    public static String lotteryID = "lottery";
    public static String lotteryFile = "Lottery.fxml";
    public static String songLottoID = "songLotto";
    public static String songLottoFile = "SongLotto.fxml";
    //spoopathon system
    public static String spoopathonID = "spoopathon";
    public static String spoopathonFile = "Spoopathon.fxml";
    //marathon system
    public static String marathonID = "marathon";
    public static String marathonFile = "Marathon.fxml";

    public static Stage stage = new Stage();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts application Sets up store and configuration Creates instance of
     * the bot but does not read chat until started Sets up ScreenController to
     * handle scene switching, default to dashboard
     */
    @Override
    public void start(Stage primaryStage) {
        //begin bot setup
        try {
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve("kfbot.xml");
            final Elements elements = configuration.parseConfiguration(xmlResolved.toString());
            store = new XmlDatastore(elements);
            //store.setStore(store);
            socket = new Socket(store.getConfiguration().host, store.getConfiguration().port);
            socket.setKeepAlive(true);
            //store.setSocket(socket);
            out = new PrintStream(socket.getOutputStream());
            //store.setOut(out);
            Charset charset = Charset.forName("UTF-8");
            //outWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), charset));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
            //store.setIn(in);

            bot = new TwitchBotX(store, in, out, socket);
            store.setBot(bot);
            //out.println("PASS " + store.getConfiguration().password);
            //out.println("NICK " + store.getConfiguration().account);
            //out.println("JOIN #" + store.getConfiguration().joinedChannel);
            messenger = new TwitchMessenger(store.getBot().getOut(), store.getConfiguration().joinedChannel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //create and show GUI
        ScreensController container = new ScreensController();
        container.loadScreen(dashboardID, dashboardFile);
        container.setScreen(dashboardID);
        // TODO resizing scale options
        Group root = new Group();
        root.getChildren().addAll(container);
        root.setStyle("-fx-font-family: \"Comfortaa\", cursive;");
        ScalableContentPane scale = new ScalableContentPane();
        Scene scene = new Scene(root);
        scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Comfortaa");
        scale.setContent(root);
        Scene testScale = new Scene(scale, 600, 400);
        testScale.getStylesheets().add("https://fonts.googleapis.com/css?family=Comfortaa");
        //stage.setScene(scene);
        stage.setScene(testScale);
        stage.setTitle("Kungfufruit Bot 2.0");
        stage.getIcons().add(new Image("http://kf.bot.raxastudios.com/kffcLove.png"));
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0));
        System.out.println("test print after open bot data: " + store.getBot().getSock().toString());
    }
}
