package com.martysh12.racecs.net;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.martysh12.racecs.RaceCS;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIUtils {
    private static final String URL_BASE = "aircs.racing";
    public static final String URL_WEBSOCKET = "wss://" + URL_BASE + "/ws";
    public static final String URL_STATIONS = "https://" + URL_BASE + "/api/stations";
    public static final String URL_PLAYERS = "https://" + URL_BASE + "/api/users";
    public static final String URL_TEAMS = "https://" + URL_BASE + "/api/teams";

    public static @Nullable Map<String, String> getStations(String locale) {
        HttpResponse<String> response = HTTP.Request.get(URL_STATIONS)
                .header("Accept-Language", locale)
                .send();

        RaceCS.logger.info("awaaga " + response.toString());

        if (response == null) {
            RaceCS.logger.error("Couldn't download stations");
            return null;
        }

        if (response.statusCode() != 200) {
            RaceCS.logger.error("Couldn't download stations (status code {}), body: {}", response.statusCode(), response.body());
            return null;
        }

        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Time for the boring part
        Map<String, String> stations = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
            stations.put(entry.getKey(), entry.getValue().getAsJsonObject().get("name").getAsString());
        }

        return stations;
    }

    public static @Nullable List<Team> getTeams() {
        HttpResponse<String> response = HTTP.Request.get(URL_TEAMS)
                .send();

        if (response == null) {
            RaceCS.logger.error("Couldn't download teams");
            return null;
        }

        if (response.statusCode() != 200) {
            RaceCS.logger.error("Couldn't download teams (status code {}), body: {}", response.statusCode(), response.body());
            return null;
        }

        if (response.body().length() == 0) {
            RaceCS.logger.info("Response body length is 0, returning empty list");
            return new ArrayList<>(); // No teams
        }

        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        // Time for the boring part, again
        List<Team> teams = new ArrayList<>();

        jsonArray.forEach(jsonElement -> teams.add(Team.fromJsonObject(jsonElement.getAsJsonObject())));

        return teams;
    }

    public static @Nullable Map<String, User> getUsers() {
        HttpResponse<String> response = HTTP.Request.get(URL_PLAYERS)
                .send();

        if (response == null) {
            RaceCS.logger.error("Couldn't download players");
            return null;
        }

        if (response.statusCode() != 200) {
            RaceCS.logger.error("Couldn't download players (status code {}), body: {}", response.statusCode(), response.body());
            return null;
        }

        if (response.body().length() == 0) {
            RaceCS.logger.info("Response body length is 0, returning empty map");
            return new HashMap<>(); // No players
        }

        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

        // Time for the boring part, again again
        Map<String, User> users = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
            users.put(entry.getKey(), User.fromJsonObject(entry.getValue().getAsJsonObject(), entry.getKey()));
        }

        return users;
    }
}
