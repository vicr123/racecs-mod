package com.martysh12.racecs.net;

import com.martysh12.racecs.RaceCS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {
    private static Map<String, User> users = new HashMap<>();
    private static final RaceCSWebsocketClient.EventListener eventListener = new RaceCSWebsocketClient.EventListener() {
        @Override
        public void onNewPlayer(String username, java.util.UUID uuid) {
            var user = new User();
            user.name = username;
            user.uuid = uuid;
            user.place = -1;
            users.put(username, user);
        }

        @Override
        public void onRemovePlayer(String username) {
            users.remove(username);
        }

        @Override
        public void onVisitation(String username, java.util.UUID uuid, String stationShortCode, String teamId) {
            var user = users.get(username);
            if (user == null) {
                RaceCS.logger.error("User {} was visited by unknown user {}", username, uuid);
                return;
            }

            user.visited.add(stationShortCode);
        }

        @Override
        public void onCompletion(String username, int place) {
            var user = users.get(username);
            if (user == null) {
                RaceCS.logger.error("User {} completed unknown user {}", username, place);
                return;
            }

            user.place = place;
        }
    };

    public static RaceCSWebsocketClient.EventListener getEventListener() {
        return eventListener;
    }

    public static void downloadUsers() {
        new Thread(() -> {
            RaceCS.logger.info("Starting download of players");
            var userList = APIUtils.getUsers();
            if (userList == null) {
                RaceCS.logger.error("Unable to download the players. Keeping last list.");
                return;
            }

            users = userList;
            RaceCS.logger.info("Downloaded players successfully. {} players loaded.", users.size());
        }, "User Download Thread").start();
    }

    public static User getUserByUsername(String username) {
        return users.get(username);
    }

    public static List<User> getUsers() {
        return List.copyOf(users.values());
    }
}
