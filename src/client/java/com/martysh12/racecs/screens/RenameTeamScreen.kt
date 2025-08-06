package com.martysh12.racecs.screens

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import net.minecraft.client.gui.DrawContext
import com.martysh12.racecs.net.Team

class RenameTeamScreen(private val parent: Screen, private val originalName: String) : Screen(Text.translatable("screen.race.rename_team_title")) {
    private lateinit var nameField: TextFieldWidget
    private lateinit var doneButton: ButtonWidget
    private lateinit var cancelButton: ButtonWidget
    private var isRenaming = false

    override fun init() {
        nameField = TextFieldWidget(textRenderer, width / 2 - 100, height / 2 - 20, 200, 20, Text.literal(""))
        nameField.text = originalName
        nameField.setMaxLength(32)

        doneButton = ButtonWidget.builder(Text.translatable("screen.race.rename_team")) { button ->
            if (nameField.text.isNotEmpty() && !isRenaming) {
                isRenaming = true
                client?.networkHandler?.sendChatCommand("racecs race setteamname \"${nameField.text}\"")
                client?.setScreen(parent)
            }
        }.dimensions(width / 2 + 5, height / 2 + 10, 95, 20).build()

        cancelButton = ButtonWidget.builder(Text.translatable("gui.cancel")) { button ->
            client?.setScreen(parent)
        }.dimensions(width / 2 - 100, height / 2 + 10, 95, 20).build()

        addDrawableChild(nameField)
        addDrawableChild(doneButton)
        addDrawableChild(cancelButton)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        context?.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 40, 0xFFFFFF)
        super.render(context, mouseX, mouseY, delta)
    }
}