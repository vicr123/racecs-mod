package com.martysh12.racecs;

import com.martysh12.racecs.net.*;
import com.martysh12.racecs.gui.toast.ToastLauncher;
import com.martysh12.racecs.screens.RaceCsScreen;
import com.martysh12.racecs.screens.ScreenManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class RaceCS implements ClientModInitializer {
    public static final String ID = "racecs";
    public static final Logger logger = LoggerFactory.getLogger(ID);
    private static final Reconnector reconnector = new Reconnector();
    private static final RaceCSWebsocketClient.EventListener eventListener = new RaceCSWebsocketClient.EventListener() {
        @Override
        public void onDisconnect(int code, String reason, boolean remote) {
            hasDisconnected = true;
        }

        @Override
        public void onStationChange() {
            RaceCS.logger.info("Stations have been changed");
            StationManager.downloadStations();
        }
    };

    private static boolean hasDisconnected = true;

    public static RaceCS INSTANCE;

    public static MinecraftClient mc;
    private static ToastLauncher toastLauncher;
    private static RaceCSWebsocketClient websocketClient;

    private KeyBinding raceButton;
    private boolean raceButtonPressed = false;

    private static ScreenManager screenManager = null;

    @Override
    public void onInitializeClient() {
        // Wait until MinecraftClient initialises (see MinecraftClientMixin.onInit)
        if (INSTANCE == null) {
            raceButton = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.racecs.open_screen", // Translation key
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_APOSTROPHE, // The ' key
                    "category.racecs.main" // Category translation key
            ));

            INSTANCE = this;
            return;
        }

        RaceCS.logger.info("Initialising {}", ID);

        // Get the Minecraft client, so we don't get it manually each time
        mc = MinecraftClient.getInstance();
        toastLauncher = new ToastLauncher(); // ToastLauncher depends on the line above

        // Set up the websocket stuff
        new Thread(reconnector, "Reconnector Thread").start();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (raceButtonPressed && !raceButton.isPressed()) {
                raceButtonPressed = false;
            } else if (!raceButtonPressed && raceButton.isPressed()) {
                raceButtonPressed = true;

                if (screenManager == null) {
                    screenManager = new ScreenManager(client);
                    websocketClient.addEventListener(screenManager.getEventListener());
                }
                screenManager.openScreen();
            }
        });
    }

    private static void createWebsocketClient() {
        try {
            websocketClient = new RaceCSWebsocketClient(new URI(APIUtils.URL_WEBSOCKET));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // This shouldn't happen to any wallaby
        }
        websocketClient.connect();

        websocketClient.addEventListener(TeamManager.getEventListener());
        websocketClient.addEventListener(UserManager.getEventListener());
        websocketClient.addEventListener(toastLauncher.getEventListener());
        websocketClient.addEventListener(eventListener);
        if (screenManager != null) {
            websocketClient.addEventListener(screenManager.getEventListener());
        }
    }

    private static class Reconnector implements Runnable {
        @Override
        public void run() {
            while (true) {
                // Re-create the websocket client if it disconnects
                if (hasDisconnected) {
                    hasDisconnected = false;
                    createWebsocketClient();
                }

                try {
                    Thread.sleep(5000); // Shut up IntelliJ
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
