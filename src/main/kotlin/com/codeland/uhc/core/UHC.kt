package com.codeland.uhc.core

import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.customSpawning.CustomSpawningType
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.Portal
import com.codeland.uhc.event.Trader
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.gen.CustomGenLayers.BORDER_INCREMENT
import com.codeland.uhc.world.gen.CustomGenLayers.OCEAN_BUFFER
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.RenderType
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

object UHC {
	private var preGameConfig: GameConfig = GameConfig()

	var game: Game? = null
	var timer = 0
	var countdownTimerGoing = false

	var teleportGroups = HashMap<UUID, Location>()
	var worldRadius: Int = 375

	var bot: MixerBot? = null
	lateinit var heartsObjective: Objective

	val areaPerPlayer = area(375.0f) / 8
	fun area(radius: Float) = ((radius * 2) + 1).pow(2)
	fun radius(area: Float) = (sqrt(area) - 1) / 2

	fun getConfig(): GameConfig {
		return game?.config ?: preGameConfig
	}

	/* game flow modifiers */

	private fun countdownColor(number: Int): TextColor {
		return Util.interpColor(1.0f - (number / 10.0f), TextColor.color(0xebd80c), TextColor.color(0xeb0c0c))
	}

	fun startLobby() {
		/* register hearts objective */
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", Component.text("hp"), RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName(Component.text("hp"))
		objective.displaySlot = DisplaySlot.PLAYER_LIST

		heartsObjective = objective

		/* clear residual teams */
		TeamData.destroyTeam(null, true, true) {}
		Bukkit.getServer().onlinePlayers.forEach { player -> Lobby.onSpawnLobby(player) }

		/* begin global ticking task */
		/* holds a centralized list of all general continuous tasks throughout the game */
		var currentTick = 0

		SchedulerUtil.everyTick {
			val currentGame = game
			if (currentGame != null) {
				val switchResult = currentGame.phase.tick(currentTick)

				if (currentGame.phase is Grace || currentGame.phase is Shrink) {
					CustomSpawning.spawnTick(CustomSpawningType.HOSTILE, currentTick, currentGame)
					CustomSpawning.spawnTick(CustomSpawningType.PASSIVE, currentTick, currentGame)
					CustomSpawning.spawnTick(CustomSpawningType.BLAZE, currentTick, currentGame)
				}

				Portal.portalTick(currentGame)
				PlayerData.zombieBorderTick(currentTick, currentGame)
				ledgerTrailTick(currentGame, currentTick)

				if (currentTick % 20 == 0) {
					currentGame.updateMobCaps(currentGame.world)
					currentGame.updateMobCaps(currentGame.otherWorld)
					containSpecs()
				}

				val halfWay = (currentGame.config.graceTime.get() + currentGame.config.shrinkTime.get()) * 20 / 2

				if (timer < halfWay) {
					currentGame.sugarCaneRegen.tick()
					currentGame.leatherRegen.tick()

				} else if (timer == halfWay) {
					Trader.deployTraders()
				}

				if (switchResult) currentGame.nextPhase()
				if (currentGame.phase !is Postgame) ++timer

			} else if (currentTick % 20 == 0 && countdownTimerGoing) {
				++timer

				if (timer < 0) {
					val countdownTitle = Title.title(
						Component.text("${-timer}", countdownColor(-timer), TextDecoration.BOLD),
						Component.text("Game starts in"),
						Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(2))
					)

					TeamData.teams.forEach { it.members.forEach { member ->
						Bukkit.getPlayer(member)?.showTitle(countdownTitle)
					}}

				} else if (timer == 0) {
					countdownTimerGoing = false

					val (gameWorld, netherWorld) = preGameConfig.getWorlds()
					if (gameWorld == null || netherWorld == null) return@everyTick

					val newGame = Game(
						preGameConfig,
						worldRadius,
						gameWorld,
						netherWorld
					)

					/* set border in each game dimension */
					listOf(gameWorld, netherWorld).forEach { world ->
						world.worldBorder.setCenter(0.5, 0.5)
						world.worldBorder.size = worldRadius * 2 + 1.0

						world.time = 0
						world.isThundering = false
						world.setStorm(false)
					}

					/* give all teams that don't have names a name */
					/* add people to team vcs */
					TeamData.teams.forEach { team ->
						if (team.name == null) team.automaticName()
						if (preGameConfig.usingBot.get()) bot?.addToTeamChannel(team, team.members)
					}

					/* teleport and set playerData to current */
					teleportGroups.forEach { (uuid, location) ->
						newGame.startPlayer(uuid, location)
					}

					game = newGame
				}
			}

			Lobby.lobbyTipsTick(currentTick)
			ArenaManager.perTick(currentTick)

			Bukkit.getOnlinePlayers().forEach { player ->
				UHCBar.updateBar(player)
			}

			/* highly composite number */
			currentTick = (currentTick + 1) % 294053760
		}
	}

	private fun ledgerTrailTick(game: Game, currentTick: Int) {
		if (currentTick % 40 != 0) return

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			val player = Bukkit.getPlayer(uuid)

			if (playerData.participating && (player == null || player.gameMode !== GameMode.SPECTATOR)) {
				val block = Action.getPlayerLocation(uuid)?.block
				if (block != null) game.ledger.tracker.addPlayerPosition(uuid, block)
			}
		}
	}

	/**
	 * @param messageStream send status messages and error messages to the caller,
	 * true indicates an error
	 */
	fun startGame(messageStream: (Boolean, String) -> Unit): Boolean {
		if (game != null) {
			messageStream(true, "Game has already started")
			return false
		}

		val numPlayers = TeamData.teams.fold(0) { acc, team -> acc + team.members.size }
		if (numPlayers == 0) {
			messageStream(true, "No one is playing")
			return false
		}

		messageStream(false, "Creating game worlds for $numPlayers player${if (numPlayers == 1) "" else "s"}")

		worldRadius = (ceil(radius(numPlayers * preGameConfig.scale.get() * areaPerPlayer) / BORDER_INCREMENT) * BORDER_INCREMENT).toInt() + OCEAN_BUFFER

		/* create worlds */
		WorldManager.refreshGameWorlds()

		/* get where players are teleporting */
		val (defaultWorld, otherWorld) = preGameConfig.getWorlds()
		if (defaultWorld == null || otherWorld == null) {
			messageStream(true, "Worlds did not initialize")
			return false
		}

		val tempTeleportLocations = PlayerSpreader.spreadPlayers(
			defaultWorld,
			TeamData.teams.size,
			worldRadius - 16.0,
			if (defaultWorld.environment === World.Environment.NETHER) {
				PlayerSpreader::findYMid
			} else {
				PlayerSpreader::findYTop
			}
		)

		if (tempTeleportLocations.isEmpty()) {
			messageStream(true, "Not enough valid starting locations found")
			return false
		}

		/* create the master map of teleport locations */
		teleportGroups = HashMap()

		TeamData.teams.forEachIndexed { i, team ->
			team.members.forEach { uuid ->
				teleportGroups[uuid] = tempTeleportLocations[i]
			}
		}

		timer = -11
		countdownTimerGoing = true
		preGameConfig.lock = true

		messageStream(false, "Starting UHC")

		return true
	}

	fun destroyGame() {
		game = null
		preGameConfig = GameConfig()

		PlayerData.prune()
		Bukkit.getOnlinePlayers().forEach { player -> Lobby.onSpawnLobby(player) }

		WorldManager.destroyGameWorlds()
	}

	fun containSpecs() {
		val currentGame = game ?: return
		val radius = currentGame.initialRadius

		Bukkit.getOnlinePlayers()
			.filter { it.world === currentGame.world || it.world === currentGame.otherWorld }
			.forEach { player ->
				if (player.gameMode == GameMode.SPECTATOR) {
					val locX = player.location.blockX.toDouble()
					val locZ = player.location.blockZ.toDouble()

					val x = when {
						locX > radius -> radius.toDouble()
						locX < -radius -> -radius.toDouble()
						else -> locX
					}

					val z = when {
						locZ > radius -> radius.toDouble()
						locZ < -radius -> -radius.toDouble()
						else -> locZ
					}

					if (x != locX || z != locZ) player.teleport(player.location.set(x + 0.5, player.location.y, z + 0.5))
				}
			}
	}
}
