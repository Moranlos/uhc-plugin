package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import java.util.*

class Betrayal(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	class BetrayalData(var swaps : Int, var kills : Int)

	override fun onEnable() {}
	override fun onDisable() {}

	override val representation: ItemStack
		get() = ItemStack(Material.BONE)

	override fun onPhaseSwitch(phase: PhaseVariant) {
		/* reset data before a game */
		if (phase.type == PhaseType.WAITING) {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				val data = PlayerData.getQuirkData<BetrayalData>(playerData, QuirkType.BETRAYAL)
				data.kills = 0
				data.swaps = 0
			}
		}
	}

	override fun defaultData(): Any {
		return BetrayalData(0, 0)
	}

	companion object {
		fun calculateScores(team : ArrayList<UUID>):ArrayList<Pair<UUID, Int>>{
			val scoreList = ArrayList<Pair<UUID, Int>>()

			team.forEach { uuid ->
				val data = PlayerData.getQuirkData<BetrayalData>(uuid, QuirkType.BETRAYAL)
				val score = data.kills - data.swaps

				scoreList.add(Pair(uuid, score))
			}

			scoreList.sortByDescending { pair -> pair.second }
			return scoreList
		}

		fun onPlayerDeath(playerUUID: UUID, killerUUID: UUID) {
			val killerTeam = TeamData.playersTeam(playerUUID)
			val playerTeam = TeamData.playersTeam(killerUUID)

			if (killerTeam == playerTeam) {
				--PlayerData.getQuirkData<BetrayalData>(killerUUID, QuirkType.BETRAYAL).kills

			} else {
				/* change scores of player and killer */
				++PlayerData.getQuirkData<BetrayalData>(playerUUID, QuirkType.BETRAYAL).swaps
				++PlayerData.getQuirkData<BetrayalData>(killerUUID, QuirkType.BETRAYAL).kills


				if (killerTeam != null) {
					val memberLocation = GameRunner.getPlayerLocation(killerTeam.members[Util.randRange(0, killerTeam.members.lastIndex)])

					TeamData.addToTeam(killerTeam, playerUUID, true)

					SchedulerUtil.nextTick {
						if (memberLocation != null) GameRunner.teleportPlayer(playerUUID, memberLocation)
					}

					if (TeamData.teams.size == 1) {
						val winningTeam = TeamData.teams[0]

						var scores = calculateScores(TeamData.teams[0].members)

						for (i in 0..winningTeam.members.lastIndex) {
							Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
								val player = Bukkit.getOfflinePlayer(scores[i].first)
								val betrayalData = PlayerData.getQuirkData<BetrayalData>(scores[i].first, QuirkType.BETRAYAL)

								GameRunner.sendGameMessage(onlinePlayer,
									"${i + 1}: ${player.name} Kills: ${betrayalData.kills} Swaps: ${betrayalData.swaps}"
								)
							}
						}

						GameRunner.uhc.endUHC(winningTeam.members)
					}
				}
			}
		}
	}
}