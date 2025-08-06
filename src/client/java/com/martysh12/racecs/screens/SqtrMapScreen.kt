package com.martysh12.racecs.screens

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class SqtrMapScreen(
    client: MinecraftClient,
    manager: ScreenManager
) : InteractiveImageScreen(Text.literal("RaceCS"), client, manager) {

    private var currentPage = 0

    override fun getImageIdentifier(): Identifier {
        // Replace with your actual mod ID and image path
        return Identifier("racecs", "textures/maps/sqtr.png")
    }

    override fun init() {
        super.init()
        manager.buildButtons(this)
    }
}