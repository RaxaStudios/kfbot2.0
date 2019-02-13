/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot;

import com.twitchbotx.gui.controllers.DashboardController;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Raxa
 */
public class WSClass extends WebSocketClient {

    private final String botName;
    private final String channelName;
    private final String oAuth;
    private final URI uri;
    private long lastPong = System.currentTimeMillis();
    private long lastPing = 0l;
    private long lastReconnect = 0;
    private final Datastore store;
    private static final String BOT_VERSION = "v2.02";

    public WSClass(URI uri, String channelName, String botName, String oAuth, Datastore store) {
        super(uri, new Draft_6455());
        this.uri = uri;
        this.channelName = channelName;
        this.botName = botName;
        this.oAuth = oAuth;
        this.store = store;
    }

    /*
     * Method that sets sockets and connects to Twitch.
     *
     * @param {boolean} reconnect
     */
    public boolean connectWSS(boolean reconnect) {
        try {
            if (reconnect) {
                System.out.println("Reconnecting to Twitch WS-IRC Server (SSL) [" + this.uri.getHost() + "]");
            } else {
                System.out.println("Connecting to Twitch WS-IRC Server (SSL) [" + this.uri.getHost() + "]");
            }
            // Get our context.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // Init the context.
            sslContext.init(null, null, null);
            // Get a socket factory.
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            // Create the socket.
            Socket socket = sslSocketFactory.createSocket();
            // Set TCP no delay.
            socket.setTcpNoDelay(true);
            // Set the socket.
            this.setSocket(socket);
            // Create a new parser instance.

            // Connect.
            this.connect();
            return true;
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
        }
        return false;
    }

    /**
     * Callback that is called when we open a connect to Twitch.
     *
     * @param {ServerHandshake} handshakedata
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // Send the oauth
        this.send("PASS " + oAuth);
        // Send the bot name.
        this.send("NICK " + botName);

        // Request our tags
        // uncomment membership for joins/parts
        //this.send("CAP REQ :twitch.tv/membership");
        this.send("CAP REQ :twitch.tv/commands");
        this.send("CAP REQ :twitch.tv/tags");

        // Join the channel.
        this.send("JOIN #" + channelName);
        // Join bot's channel to accept admin messages
        this.send("JOIN #" + botName);
        
        // Create a new ping timer that runs every 30 seconds.
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Thread.currentThread().setName("WSClass::pingTimer");

            // if we sent a ping longer than 3 minutes ago, send another one.
            if (System.currentTimeMillis() > (lastPing + 180000)) {
                lastPing = System.currentTimeMillis();
                this.send("PING");
            }

            // If Twitch's last pong was more than 3.5 minutes ago, close our connection.
            if (System.currentTimeMillis() > (lastPong + 210000)) {
                System.out.println("Closing our connection with Twitch since no PONG got sent back.");
                this.close();
            }
        }, 10, 30, TimeUnit.SECONDS);

        final String ReadyMessage = BOT_VERSION + " has joined the channel.";
        sendMessage(ReadyMessage, true);
    }

    /**
     * Callback that is called when the connection with Twitch is lost.
     *
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("On Close: code: " + code + " reason: " + reason + " remote: " + remote);
        //reconnect here
        reconnect();
    }

    /**
     * Callback that is called when we get an error from the socket.
     *
     * @param ex
     */
    @Override
    public void onError(Exception ex) {
        System.out.println("Error: " + ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Callback that is called when we get a message from Twitch.
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        if (message.startsWith("PING")) {
            this.send("PONG");
        } else if (message.startsWith("PONG")) {
            lastPong = System.currentTimeMillis();
        } else if (message.contains("JOIN #" + channelName)){
            //tool for checking joins/parts, possible future use with points
            String name = "";
            name = message.substring(message.indexOf(":") + 1, (message.indexOf("!",message.indexOf(":"))));
        } else {
            message  = message.replaceAll("\\r\\n|\\r|\\n", "");
            parse(message);
        }
    }

    void parse(String msg) {
        CommandParser parser = new CommandParser(store);
        parser.parse(msg);
    }

    /**
     * Check for fragment data
     *
     * @param frame
     */
    @Override
    public void onFragment(Framedata frame) {
        System.out.println("Frame Data: " + frame.getPayloadData().toString());
    }

    /*
     * Method that handles reconnecting with Twitch.
     */
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public void reconnect() {

        // Variable that will break the reconnect loop.
        boolean reconnected = false;

        while (!reconnected) {
            if (lastReconnect + 10000 <= System.currentTimeMillis()) {
                lastReconnect = System.currentTimeMillis();
                try {
                    // Close the connection and destroy the class.
                    this.close();
                    // Create a new connection.
                    DashboardController.wIRC = new WSClass(new URI("wss://irc-ws.chat.twitch.tv"), channelName, botName, oAuth, store);
                    // Check if we are reconnected.
                    reconnected = DashboardController.wIRC.connectWSS(true);
                } catch (URISyntaxException ex) {
                    System.out.println("Error when reconnecting to Twitch [" + ex.getClass().getSimpleName() + "]: " + ex.getMessage());
                }
            }
            // Sleep for 5 seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.out.println("Sleep failed during reconnect [InterruptedException]: " + ex.getMessage());
            }
        }
    }

    public boolean sendMessage(String message, boolean action) {
        if (action) {
            message = "/me " + message;
        }
        String channel = store.getConfiguration().joinedChannel;
        this.send("PRIVMSG #"
                + channel
                + " "
                + ":"
                + message);
        return true;
    }
    public void sendNonFormatMessage (String message){
        this.send(message);
    }
    
    public void sendEditorMessage(String message){
        if(message.equals("")){ return; }
        String channel = store.getConfiguration().account;
        this.send("PRIVMSG #"
                + channel
                + " "
                + ":"
                + message);
    }
}
