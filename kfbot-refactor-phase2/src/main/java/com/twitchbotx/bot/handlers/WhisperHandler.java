/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Raxa
 */
public class WhisperHandler {

    private static WebSocket connection = null;

    private final Datastore store;
    static String botID;
    static String botWhisperToken;
    static int pongCounter;

    public WhisperHandler(final Datastore store) {
        this.store = store;
        this.botID = store.getConfiguration().botID;
        this.botWhisperToken = store.getConfiguration().botWhisperToken;
    }

    public static void connect(final Datastore store,
            final PrintStream outstream) {
        try {
            connection = new WebSocketFactory().createSocket("wss://pubsub-edge.twitch.tv").addListener(new PubSubListener(store, outstream)).connect();
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> connection.sendText("{ \"type\": \"PING\" }"), 5, 5, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> checkPongs(store, outstream), 21, 20, TimeUnit.MINUTES);
    }

    private static void checkPongs(final Datastore store,
            final PrintStream outstream) {
        if (pongCounter < 2) {
            connection.sendClose();
            System.out.println("Attemping to reconnect to whisper PUBSUB");
            WhisperHandler.connect(store, outstream);
        } else {
            pongCounter = 0;
        }
    }

    private static class PubSubListener extends WebSocketAdapter {

        private final Datastore store;

        private PrintStream outstream;
        
        private final TwitchMessenger messenger;

        public PubSubListener(final Datastore store,
                final PrintStream outstream) {
            this.store = store;
            this.outstream = outstream;
            this.messenger = new TwitchMessenger(outstream, store.getConfiguration().joinedChannel);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            System.out.println("Whisper PubSub connected");

            //start bot whispers
            ObjectNode botNode = JsonNodeFactory.instance.objectNode();
            botNode.putArray("topics").add("whispers." + botID);
            botNode.put("auth_token", botWhisperToken);
            ObjectNode botRootNode = JsonNodeFactory.instance.objectNode();
            botRootNode.put("type", "LISTEN");
            botRootNode.set("data", botNode);
            websocket.sendText(botRootNode.toString());
            //System.out.println(botRootNode.toString());
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            try {
                JsonNode root = new ObjectMapper().readTree(text);
                System.out.println(root.textValue());
                System.out.println(root.toString());
                if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                    String topic = root.get("data").get("topic").asText();
                    if (topic.equalsIgnoreCase("whispers." + botID)) {
                        JsonNode messageNode = new ObjectMapper().readTree(root.get("data").get("message").asText());
                        JsonNode dataNode = new ObjectMapper().readTree(messageNode.get("data").asText());
                        String userId = dataNode.get("from_id").asText();
                        String userMessage = (dataNode.get("body").asText());
                        String displayName = dataNode.get("tags").get("display_name").asText();
                        System.out.println(userMessage + " " + displayName);
                        if (!displayName.equals(store.getConfiguration().account)) {
                            if (displayName.equalsIgnoreCase("Raxa") || displayName.equals(store.getConfiguration().joinedChannel)) {
                                displayName = displayName.toLowerCase();
                                CommandOptionHandler ch = new CommandOptionHandler(store);
                                int choice = 0;
                                if (userMessage.contains("!filter-add")) {
                                    choice = 1;
                                } else if (userMessage.contains("!filter-delete")) {
                                    choice = 2;
                                } else if (userMessage.contains("!filter-reason")) {
                                    choice = 3;
                                }
                                switch (choice) {
                                    case 1:
                                        //ch.filterAdd(userMessage, displayName);
                                        break;
                                    case 2:
                                        //ch.filterDel(userMessage, displayName);
                                        break;
                                    case 3:
                                        //ch.filterReason(userMessage, displayName);
                                        break;
                                }
                            } else if (displayName.equals(store.getConfiguration().account)) {
                                return;
                            } else {
                                messenger.sendWhisper(".w Raxa whisper to kfbot from " + displayName + " message: " + userMessage);
                            }
                        }
                    }
                } else if (root.get("type").asText().contains("PONG")) {
                    pongCounter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) {
            System.out.println("***PubSub Error Message: " + new String(data));
        }

        @Override
        public void onFrame(WebSocket websocket, WebSocketFrame frame) {
            if (!frame.getPayloadText().contains("\"type\": \"PONG\"")) {
                //System.out.println("PubSub Frame: " + frame.getPayloadText());
            }
            //Ignore pong responses for now, correct behavior would be to time the ping/pong difference and reconnect if no pong response 10 seconds after ping.
        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) {
            System.out.println("PubSub Ping Frame" + frame);
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
            connect(store, outstream);
            System.out.println("**PubSub Error: " + cause.toString());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
            System.out.println("PubSub disconnected, by server: " + closedByServer);
        }
    }
}
