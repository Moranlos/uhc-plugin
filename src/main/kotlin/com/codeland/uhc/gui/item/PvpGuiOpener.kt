package com.codeland.uhc.gui.item

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PvpGuiOpener : CommandItem() {
	val MATERIAL = Material.IRON_SWORD

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.displayName(Util.gradientString("Lobby PVP menu", TextColor.color(0xe80e0e), TextColor.color(0xe8c00e)))
		meta.lore(listOf(Component.text("Right click to to open the Lobby PVP Menu")))

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		PlayerData.getPlayerData(player.uniqueId).lobbyPvpGui.open(player)
	}
}
