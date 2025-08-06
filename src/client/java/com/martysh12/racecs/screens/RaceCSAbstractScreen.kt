package com.martysh12.racecs.screens

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text

abstract class RaceCSAbstractScreen(title: Text) : Screen(title) {
    fun addButtonChild(button: ButtonWidget) {
        addDrawableChild(button)
    }
}