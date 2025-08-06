package com.martysh12.racecs.screens

import com.martysh12.racecs.net.StationManager
import com.martysh12.racecs.net.Team
import com.martysh12.racecs.net.TeamManager
import com.martysh12.racecs.net.User
import com.martysh12.racecs.net.UserManager
import kotlinx.coroutines.sync.Mutex
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipPositioner
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.server.PlayerManager
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Formatting

@Environment(EnvType.CLIENT)
class RaceCsScreen(private val client: MinecraftClient, private val manager: ScreenManager) : RaceCSAbstractScreen(Text.literal("RaceCS")) {
    private var currentPage = 0
    private var leaderboard: ArrayList<User> = ArrayList()
    private var teamLeaderboard: List<Team> = ArrayList()
    private var missingStations: ArrayList<String> = ArrayList()
    private var stations: List<String> = ArrayList()
    private var user: User? = null
    private var currentTooltip: List<OrderedText>? = null
    private var registerButton =
        ButtonWidget.builder(Text.translatable("screen.race.join")) {
            client.networkHandler!!.sendChatCommand("racecs race register")
        }
            .dimensions(width / 2 - 100, height / 2 + 30, 200, 20)
            .build()
    private var renameTeamButton =
        ButtonWidget.builder(Text.translatable("screen.race.rename_team")) {
            val team = TeamManager.getPlayerTeam(client.player?.name?.string ?: "")
            if (team != null) {
                // Open text input screen for new team name
                client.setScreen(RenameTeamScreen(this, team.name))
            }
        }
            .dimensions(280, 70, 100, 20)
            .build()
    private val lock = Any() // Object for synchronization

    override fun init() {
        super.init()

        registerButton.setPosition(width / 2 - 100, height / 2 + 30)

        manager.buildButtons(this)
        updatePage()
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        synchronized(lock) {
            super.render(context, mouseX, mouseY, delta)

            if (user == null) {
                context?.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.translatable("screen.race.not_available"),
                    width / 2,
                    height / 2,
                    0xffffff
                )
                return
            }

            if (currentTooltip != null) {
                // Set the tooltip
                setTooltip(currentTooltip)
            }

            val team = TeamManager.getPlayerTeam(client.player?.name?.string ?: "")

            // Draw the leaderboard
            context?.drawTextWithShadow(textRenderer, Text.translatable("screen.race.leaderboard"), 10, 30, 0xffffff)
            if (team != null) {
                context?.drawTextWithShadow(
                    textRenderer,
                    Text.translatable("screen.race.leaderboard.your_team"),
                    10,
                    50,
                    0xffffff
                )
                context?.drawTextWithShadow(textRenderer, Text.literal(team.name), 10, 70, 0xffffff)
                context?.drawTextWithShadow(textRenderer, teamStatus(team), 10, 90, 0xffffff)

                for (i in 0 until team.players.size) {
                    context?.drawTextWithShadow(
                        textRenderer,
                        Text.literal("- " + team.players[i]),
                        10,
                        110 + i * 20,
                        0xffffff
                    )
                }

                val offset = team.players.size * 20

                context?.drawTextWithShadow(
                    textRenderer,
                    Text.translatable("screen.race.leaderboard.other_teams"),
                    10,
                    130 + offset,
                    0xffffff
                )
                var row = 0
                for (i in 0 until teamLeaderboard.size) {
                    val thisTeam = teamLeaderboard[i]
                    if (thisTeam == team) continue
                    context?.drawTextWithShadow(
                        textRenderer,
                        Text.literal(thisTeam.name),
                        10,
                        150 + 30 * row + 20 + offset,
                        0xffffff
                    )
                    context?.drawTextWithShadow(
                        textRenderer,
                        teamStatus(thisTeam),
                        10,
                        150 + 30 * row + 40 + offset,
                        0xffffff
                    )
                    row++
                }
            } else {
                context?.drawTextWithShadow(
                    textRenderer,
                    Text.translatable("screen.race.leaderboard.name"),
                    40,
                    50,
                    0xffffff
                )
                context?.drawTextWithShadow(
                    textRenderer,
                    Text.translatable("screen.race.leaderboard.visited"),
                    280,
                    50,
                    0xffffff
                )
                for (i in 0 until leaderboard.size) {
                    val user = leaderboard[i]
                    context?.drawTextWithShadow(
                        textRenderer,
                        Text.literal(if (user.place == -1) "---" else user.place.toString()),
                        10,
                        70 + 20 * i,
                        0xffffff
                    )
                    context?.drawTextWithShadow(
                        textRenderer,
                        Text.literal(user.name.toString()),
                        40,
                        70 + 20 * i,
                        0xffffff
                    )
                    context?.drawTextWithShadow(
                        textRenderer,
                        Text.literal(user.visited.size.toString()),
                        280,
                        70 + 20 * i,
                        0xffffff
                    )
                }
            }

            // Draw the stations
            context?.drawTextWithShadow(textRenderer, Text.translatable("screen.race.remaining"), 400, 30, 0xffffff)
            context?.drawTextWithShadow(textRenderer, Text.literal(missingStations.size.toString()), 500, 30, 0xffffff)

            if (width - 460 < 0) return
            val cols = (width - 460) / 40
            for (i in 0 until stations.size) {
                val station = stations[i]
                val col = i % cols
                val row = i / cols

                val textColor = if (team != null) {
                    if (user!!.visited.contains(station)) 0x00ff00 else if (team.visited.contains(station)) 0x009000 else 0xffffff
                } else {
                    if (user!!.visited.contains(station)) 0x00ff00 else 0xffffff
                }
                context?.drawTextWithShadow(
                    textRenderer,
                    Text.literal(station),
                    400 + 40 * col,
                    70 + 20 * row,
                    textColor
                )
            }
        }
    }

    fun teamStatus(team: Team): Text {
        if (team.place != -1) return Text.translatable("screen.race.leaderboard.team_finished_place", team.place)
        if (team.visited.size == StationManager.getStations().size) {
            return Text.translatable("screen.race.leaderboard.team_returning", team.returned.size, team.players.size)
        } else {
            return Text.translatable("screen.race.leaderboard.team_stations", team.visited.size, StationManager.getStations().size)
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        super.mouseMoved(mouseX, mouseY)

        if (width - 460 < 0) {
            currentTooltip = null
            return
        }

        val team = TeamManager.getPlayerTeam(client.player?.name?.string ?: "")

        // Calculate the same grid positions as in render
        val cols = (width - 460) / 40

        val claimed = Text.translatable("station.state.claimed").formatted(Formatting.GREEN)
        val partiallyClaimed = Text.translatable("station.state.partially_claimed").formatted(Formatting.DARK_GREEN)
        val unclaimed = Text.translatable("station.state.unclaimed").formatted(Formatting.RED)
        val triggersNotification = Text.translatable("station.state.triggers_notification").formatted(Formatting.RESET)
        val doesNotTriggerNotification = Text.translatable("station.state.does_not_trigger_notification").formatted(Formatting.RESET)

        for (i in stations.indices) {
            val station = stations[i]
            val col = i % cols
            val row = i / cols

            // Define the area where the station text is
            val x = 400 + 40 * col
            val y = 70 + 20 * row

            // Check if mouse is in the station text area
            if (mouseX >= x && mouseX <= x + 40 &&
                mouseY >= y && mouseY <= y + 20) {

                // Create tooltip text - you can customize this
                val tooltipLines = ArrayList<OrderedText>()
                val stationName = StationManager.getStationFullName(station)
                tooltipLines.add(Text.literal(stationName).formatted(Formatting.BOLD).asOrderedText())
                if (team != null) {
                    if (user!!.visited.contains(station)) {
                        tooltipLines.add(claimed.asOrderedText())
                    }
                    else if (team.visited.contains(station)) {
                        tooltipLines.add(partiallyClaimed.asOrderedText())
                    }
                    else {
                        tooltipLines.add(unclaimed.asOrderedText())
                    }
                } else {
                    if (user!!.visited.contains(station)) {
                        tooltipLines.add(claimed.asOrderedText())
                    }
                    else {
                        tooltipLines.add(unclaimed.asOrderedText())
                    }
                }
                currentTooltip = tooltipLines
                return
            }
        }

        currentTooltip = null
    }

    fun updatePage() {
        synchronized(lock) {
            this.remove(registerButton)
            this.remove(renameTeamButton)

            user = UserManager.getUserByUsername(client.player?.name?.string ?: "")
            if (user == null) {
                this.addButtonChild(registerButton)
                return
            }

            stations = StationManager.getStations().sorted()

            val team = TeamManager.getPlayerTeam(client.player?.name?.string ?: "")
            if (team != null) {
                this.addButtonChild(renameTeamButton)
                missingStations = StationManager.getStations()
                    .filter { station -> !team.visited.contains(station) } as ArrayList<String>

                teamLeaderboard = TeamManager.getTeams().sortedWith { a, b ->
                    (a.place * 1000 + (a.visited.size)) - (b.place * 1000 + (b.visited.size))
                }
            } else {
                missingStations = StationManager.getStations()
                    .filter { station -> !user!!.visited.contains(station) } as ArrayList<String>

                leaderboard = ArrayList(UserManager.getUsers().sortedWith { a, b ->
                    if (a.place == b.place) b.visited.size - a.visited.size else a.place - b.place
                })
            }
        }
    }

    override fun resize(client: MinecraftClient?, width: Int, height: Int) {
        super.resize(client, width, height)
        registerButton.setPosition(width / 2 - 100, height / 2 + 30)
    }
}