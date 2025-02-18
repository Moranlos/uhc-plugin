package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import kotlin.collections.ArrayList
import com.codeland.uhc.gui.ItemCreator
import org.bukkit.Location
import org.bukkit.NamespacedKey

enum class KillReward(val prettyName: String, val representation: Material, val lore: Array<String>, val apply: (UUID, ArrayList<UUID>, Location) -> Unit) {
	ABSORPTION("Absorption", Material.SHIELD, arrayOf(
		"Gain 3 absorption hearts on kill",
		"Increased to 4 if alone",
		"1 absorption heart to teammates"
	), { uuid, team, _ ->
		forPlayer(uuid, team) { alone, player, others ->
			player.absorptionAmount += if (alone) 8 else 6
			others.forEach { it.absorptionAmount += 2 }
		}
	}),
	REGENERATION("Regeneration", Material.GHAST_TEAR, arrayOf(
		"Regain 3 hearts on kill",
		"Increased to 4 if alone",
		"1 heart to teammates"
	), { uuid, team, _ ->
		forPlayer(uuid, team) { alone, player, others ->
			player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, if (alone) 400 else 300, 0, false, true, true))
			others.forEach { it.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 100, 0, false, true, true))}
		}
	}),
	APPLE("Apple Drop", Material.GOLDEN_APPLE, arrayOf(
		"Killed players drop a special golden apple",
		"6 absorption hearts",
		"Regenerates for 2 hearts"
	), { _, _, location ->
		location.world.dropItem(
			location,
			ItemCreator.fromType(Material.ENCHANTED_GOLDEN_APPLE)
				.setData(uhcAppleKey, 1)
				.name("${org.bukkit.ChatColor.GOLD}UHC Apple")
				.lore("6 absorption hearts", "2 regeneration hearts")
				.create()
		)
	}),
	NONE("None", Material.NETHER_WART, arrayOf(
		"No reward on kill"
	), { _, _, _ ->

	});

	companion object {
		val uhcAppleKey = NamespacedKey(UHCPlugin.plugin, "_U_ua")

		fun forPlayer(uuid: UUID, team: ArrayList<UUID>, on: (Boolean, Player, List<Player>) -> Unit) {
			val player = Bukkit.getPlayer(uuid) ?: return
			val otherPlayers = team.filter { it != uuid }.mapNotNull { Bukkit.getPlayer(it) }
			val alone = otherPlayers.isEmpty()

			on(alone, player, otherPlayers)
		}
	}
}
