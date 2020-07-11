package com.codeland.uhc.phases.shrink

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World

class ShrinkDefault : Phase() {

	var minRadius : Double? = null

	override fun perSecond(secondsLeft: Long) {
		GameRunner.uhc.updateMobCaps()
	}

	override fun updateActionBar(remainingSeconds: Long) {
		val text = "reaching ${minRadius!!.toLong()} in ${getRemainingTimeString(remainingSeconds)}"

		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.sendActionBar("${ChatColor.GOLD}${ChatColor.BOLD}${getCountdownString()} ${player.world.worldBorder.size.toLong() / 2} $text")
		}
	}

	override fun customStart() {
		for (w in Bukkit.getServer().worlds) {
			w.setGameRule(GameRule.NATURAL_REGENERATION, false)
			w.pvp = true
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "Grace period has ended!")
		}

		minRadius = uhc.endRadius
		for (w in Bukkit.getServer().worlds) {
			if (w.environment == World.Environment.NETHER && uhc.netherToZero) {
				w.worldBorder.setSize(0.0, length)
			} else {
				w.worldBorder.setSize(minRadius!! * 2.0, length)
			}
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendPlayer(player, "The border is now shrinking")
		}
	}

	override fun getCountdownString(): String {
		return "Border radius:"
	}

	override fun endPhrase(): String {
		return "BORDER STOPPING"
	}
}
