package com.martysh12.racecs.screens

import com.martysh12.racecs.RaceCS
import com.martysh12.racecs.net.RaceCSWebsocketClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import java.util.*


class ScreenManager(val client: MinecraftClient) {
    private var currentScreen = 0
    private val raceScreen = RaceCsScreen(client, this)
    private val aircsMapScreen = AircsMapScreen(client, this)
    private val sqtrMapScreen = SqtrMapScreen(client, this)
    private val cly1Screen = Cly1Screen(client, this)
    private val cly2Screen = Cly2Screen(client, this)

    private val eventListener: RaceCSWebsocketClient.EventListener = object : RaceCSWebsocketClient.EventListener {
        override fun onEvent() {
            super.onEvent()

            raceScreen.updatePage()
        }
    }

    fun getEventListener(): RaceCSWebsocketClient.EventListener {
        return eventListener
    }

    fun openScreen() {
        client.setScreen(when (currentScreen) {
            0 -> raceScreen
            1 -> aircsMapScreen
            2 -> sqtrMapScreen
            3 -> cly1Screen
            4 -> cly2Screen
            else -> raceScreen
        })
    }

    fun openScreen(screen: Int) {
        currentScreen = screen
        openScreen()
    }

    fun buildButtons(screen: RaceCSAbstractScreen) {
        val totalButtons = 5
        val screenWidth = client.window.scaledWidth
        val buttonWidth = screenWidth / totalButtons
        val raceButton = ButtonWidget.builder(Text.translatable("screen.race"), ::openRacePage)
            .dimensions(0, 0, buttonWidth, 20)
            .build()
        val aircsButton = ButtonWidget.builder(Text.translatable("screen.map.aircs"), ::openAircsPage)
            .dimensions(buttonWidth, 0, buttonWidth, 20)
            .build()
        val sqtrButton = ButtonWidget.builder(Text.translatable("screen.map.sqtr"), ::openSqtrPage)
            .dimensions(buttonWidth * 2, 0, buttonWidth, 20)
            .build()
        val cly1Button = ButtonWidget.builder(Text.translatable("screen.map.cly1"), ::openCly1Page)
            .dimensions(buttonWidth * 3, 0, buttonWidth, 20)
            .build()
        val cly2Button = ButtonWidget.builder(Text.translatable("screen.map.cly2"), ::openCly2Page)
            .dimensions(buttonWidth * 4, 0, buttonWidth, 20)
            .build()

        screen.addButtonChild(raceButton)
        screen.addButtonChild(aircsButton)
        screen.addButtonChild(sqtrButton)
        screen.addButtonChild(cly1Button)
        screen.addButtonChild(cly2Button)
    }

    fun openRacePage(button: ButtonWidget) {
        openScreen(0)
    }

    fun openAircsPage(button: ButtonWidget) {
        openScreen(1)
    }

    fun openSqtrPage(button: ButtonWidget) {
        openScreen(2)
    }

    fun openCly1Page(button: ButtonWidget) {
        openScreen(3)
    }

    fun openCly2Page(button: ButtonWidget) {
        openScreen(4)
    }
}