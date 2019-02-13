/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.controllers.PollFeatureController;
import com.twitchbotx.gui.guiHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Basic chat based poll system opened from PollFeature.fxml Future additions
 * may include: timer, mod chat commands, on screen
 */
public class PollHandler {

    private boolean contains;
    private LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
    private List<String> usernames = new ArrayList<>();
    private String winner = "";
    private int highest = 0;
    private boolean tie = false;
    private StringBuilder winners = new StringBuilder();
    public static boolean running = false;
    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private Future f;

    public PollHandler() {

    }

    public boolean startPoll(List<String> options, int interval) {
        running = true;
        usernames.clear();
        map.clear();
        // send message to chat giving options
        StringBuilder sb = new StringBuilder();
        sb.append("A poll has started in chat! Type one of the follow options (without the brackets) [");
        for (String o : options) {
            map.put(o, 0);
            sb.append(o + "], [");
        }

        sb.delete(sb.lastIndexOf(", "), sb.length());
        sb.append(" to vote!");
        String sendText = sb.toString();

        try {
            DashboardController.wIRC.sendMessage(sendText, true);
            System.out.println("Poll open");
            sendEvent("Poll opened");
        } catch (Exception e) {
            sendEvent("Poll opened, bot not connected to chat");
            e.printStackTrace();
            return false;
        }
        if (interval > 0) {
            startReminder(map, interval);
        }
        return true;
    }

    public void startReminder(LinkedHashMap<String, Integer> currMap, int interval) {
        System.out.println("Starting reminders");
        StringBuilder sr = new StringBuilder();
        sr.append("A poll is running in chat! Type one of the follow options (without the brackets) [");
        currMap.entrySet().forEach((m) -> {
            sr.append(m.getKey() + "], [");
            System.out.println("Starting reminders m value:" + m.getKey());
        });
        sr.delete(sr.lastIndexOf(", "), sr.length());
        sr.append(" to vote!");
        System.out.println("Pre-start running: " + getRunning());
        Runnable runnable = () -> {
            System.out.println("Running value: " + getRunning());
            if (getRunning()) {
                System.out.println("running, sending " + sr.toString());
                DashboardController.wIRC.sendMessage(sr.toString(), true);
            } else {
                System.out.println("running false");
                this.shutdown();
            }
        };
        f = ses.scheduleWithFixedDelay(runnable, interval, interval, TimeUnit.SECONDS);
    }

    public void shutdown() {
        ses.shutdown();
        f.cancel(true);
        try {
            if (!ses.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                ses.shutdownNow();
                Thread.currentThread().join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            ses.shutdownNow();
        }
    }

    public boolean endPoll() {
        running = false;
        try {
            DashboardController.wIRC.sendMessage("Poll has closed", true);
        } catch (Exception e) {
            sendEvent("Poll closed, bot not connected to chat");
            e.printStackTrace();
        }
        return true;
    }

    public void drawWinner() {
        running = false;
        try {
            DashboardController.wIRC.sendMessage(getResults(), true);
        } catch (Exception e) {
            sendEvent("Poll closed, bot not connected to chat");
            e.printStackTrace();
        }
    }

    public String getResults() {
        winners.append("There has been a tie between: ");
        highest = 0;
        winner = "";
        map.entrySet().forEach((m) -> {
            if (m.getValue() > highest) {
                highest = m.getValue();
                winner = m.getKey();
                tie = false;
                highest = m.getValue();
                winner = m.getKey();
                winners.append(m.getKey() + " and ");
            } else if (m.getValue() == highest) {
                tie = true;
                winners.append(m.getKey() + " and ");
            }
        });
        winners.delete(winners.lastIndexOf("and "), winners.length());
        winners.append(" at " + highest + " votes each!");
        if (tie) {
            sendEvent(winners.toString());
            return winners.toString();
        } else {
            sendEvent("Winning choice: " + winner);
            return ("Winning choice: " + winner);
        }
    }

    public LinkedHashMap<String, Integer> getMap() {
        return this.map;
    }

    public boolean getRunning() {
        return running;
    }

    public synchronized boolean addVote(String option, String username) {
        if (usernames.contains(username)) {
            if (!username.equals("")) {
                sendEvent("Duplicate vote from: " + username + ", choice: " + option);
                return false;
            }
        }

        map.entrySet().forEach((m) -> {
            System.out.println("user: " + username + " m:" + m.getKey());
            if (option.equalsIgnoreCase(m.getKey())) {
                // if found increase point by 1
                System.out.println("before: " + m.getValue());
                int points;
                points = m.getValue() + 1;
                m.setValue(points);
                System.out.println("after: " + m.getValue());
            }
        });
        if (!username.equals("")) {
            usernames.add(username);
            System.out.println(usernames);
        }
        PollFeatureController pf = new PollFeatureController();
        pf.showStats();
        return true;
    }

    public boolean containsKey(String msg) {
        contains = false;
        map.entrySet().forEach((m) -> {
            if (m.getKey().equalsIgnoreCase(msg)) {
                contains = true;
            }
        });
        return contains;
    }

    public void addNumericVote(String option, String username) {
        Set<String> keys = map.keySet();
        int choice = Integer.parseInt(option);
        int i = 1;
        for (String o : keys) {
            System.out.println("o=" + o);
            System.out.println("i=" + i + " c=" + choice);
            if (i == choice) {
                System.out.println("equals o=" + o);
                option = o;
                i++;
            } else {
                i++;
            }
        }
        usernames.add(username);
        addVote(option, "");
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

}
