package com.martysh12.racecs.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.martysh12.racecs.RaceCS;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RaceCSWebsocketClient extends WebSocketClient {
    private final List<EventListener> eventListeners = new ArrayList<>();

    public RaceCSWebsocketClient(URI uri) throws URISyntaxException {
        super(uri);
        setConnectionLostTimeout(0);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        RaceCS.logger.info("Connected to the RaceCS websocket endpoint successfully");

        // Start downloading stations & teams only when we connect successfully to the websocket
        TeamManager.downloadTeams();
        StationManager.downloadStations();
        UserManager.downloadUsers();
    }

    @Override
    public void onMessage(String message) {

        // Parse the message
        JsonObject json;
        try {
            json = JsonParser.parseString(message).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            RaceCS.logger.error("Malformed JSON: " + message);
            return;
        }

        // Do stuff
        String type = json.get("type").getAsString();

        switch (type) {
            case "ping" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onPing();
            }
            case "collision" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onCollision(json.get("player1").getAsString(), json.get("player2").getAsString());
            }
            case "visitation" -> {
                for (EventListener eventListener : eventListeners) {
                    JsonElement team = json.get("team");
                    eventListener.onVisitation(json.get("user").getAsString(), UUID.fromString(json.get("uuid").getAsString()), json.get("station").getAsString(), team.isJsonNull() ? null : team.getAsString());
                }
            }
            case "newPlayer" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onNewPlayer(json.get("user").getAsString(), UUID.fromString(json.get("uuid").getAsString()));
            }
            case "removePlayer" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onRemovePlayer(json.get("user").getAsString());
            }
            case "stationChange" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onStationChange();
            }
            case "completion" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onCompletion(json.get("username").getAsString(), json.get("place").getAsInt());
            }
            case "completion-partial" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onCompletionPartial(json.get("player").getAsString(), json.get("team").getAsString(), json.get("teamId").getAsString(), json.get("remaining").getAsInt());
            }
            case "completion-team" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onCompletionTeam(json.get("player").getAsString(), json.get("team").getAsString(), json.get("teamId").getAsString(), json.get("place").getAsInt());
            }
            case "teamRename" -> {
                for (EventListener eventListener : eventListeners)
                    eventListener.onTeamRename(json.get("team").getAsString(), json.get("name").getAsString());
            }
            case "teaming" -> {
                List<Team> teams = new ArrayList<>();
                json.get("teams").getAsJsonArray().forEach(jsonElement -> teams.add(Team.fromJsonObject(jsonElement.getAsJsonObject())));

                for (EventListener eventListener : eventListeners)
                    eventListener.onTeaming(teams);
            }
            default -> RaceCS.logger.warn("Unknown message type \"{}\", with message {}", type, message);
        }

        for (EventListener eventListener : eventListeners)
            eventListener.onEvent();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        RaceCS.logger.error("RaceCS connection closed by " + (remote ? "remote" : "client") + " (code " + code + "), reason: " + reason);

        for (EventListener eventListener : eventListeners)
            eventListener.onDisconnect(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void addEventListener(EventListener listener) {
        eventListeners.add(listener);
    }

    public interface EventListener {
        default void onDisconnect(int code, String reason, boolean remote) {}

        default void onPing() {}
        default void onCollision(String player1, String player2) {}
        default void onVisitation(String user, UUID uuid, String station, String team) {}
        default void onNewPlayer(String user, UUID uuid) {}
        default void onRemovePlayer(String user) {}
        default void onStationChange() {}
        default void onCompletion(String username, int place) {}
        default void onCompletionPartial(String player, String team, String teamId, int remaining) {}
        default void onCompletionTeam(String player, String team, String teamId, int place) {}
        default void onTeamRename(String teamId, String name) {}
        default void onTeaming(List<Team> teams) {}

        default void onEvent() {}
    }
}
