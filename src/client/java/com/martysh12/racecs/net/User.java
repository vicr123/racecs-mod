package com.martysh12.racecs.net;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    public UUID uuid;
    public String name;
    public List<String> visited = new ArrayList<>();
    public int place;

    public static User fromJsonObject(JsonObject jsonObject, String name) {
        var user = new User();
        user.uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        user.place = jsonObject.get("place").getAsInt();
        user.name = name;

        if (jsonObject.has("visited"))
            jsonObject.get("visited").getAsJsonArray().forEach(jsonElement -> user.visited.add(jsonElement.getAsString()));

        return user;
    }
}
