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
        val raceButton = ButtonWidget.builder(Text.translatable("screen.race"), ::openRacePage).build()
        val aircsButton = ButtonWidget.builder(Text.translatable("screen.map.aircs"), ::openAircsPage).build()
        val sqtrButton = ButtonWidget.builder(Text.translatable("screen.map.sqtr"), ::openSqtrPage).build()
        val cly1Button = ButtonWidget.builder(Text.translatable("screen.map.cly1"), ::openCly1Page).build()
        val cly2Button = ButtonWidget.builder(Text.translatable("screen.map.cly2"), ::openCly2Page).build()

        val totalButtons = 5
        val screenWidth = client.window.scaledWidth
        val buttonWidth = screenWidth / totalButtons

        raceButton.setPosition(0, 0)
        raceButton.setDimensions(buttonWidth, 20)
        screen.addButtonChild(raceButton)

        aircsButton.setPosition(buttonWidth, 0)
        aircsButton.setDimensions(buttonWidth, 20)
        screen.addButtonChild(aircsButton)

        sqtrButton.setPosition(buttonWidth * 2, 0)
        sqtrButton.setDimensions(buttonWidth, 20)
        screen.addButtonChild(sqtrButton)

        cly1Button.setPosition(buttonWidth * 3, 0)
        cly1Button.setDimensions(buttonWidth, 20)
        screen.addButtonChild(cly1Button)

        cly2Button.setPosition(buttonWidth * 4, 0)
        cly2Button.setDimensions(buttonWidth, 20)
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