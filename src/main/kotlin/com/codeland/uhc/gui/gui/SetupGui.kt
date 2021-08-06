package com.codeland.uhc.gui.gui

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.world.WorldGenOption
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.format.TextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SetupGui : GuiPage(5, Util.gradientString("UHC Setup", TextColor.color(0x34ebb4), TextColor.color(0x093c80))) {

	private val quirkToggles = Array(QuirkType.values().size) { i -> QuirkToggle(i, QuirkType.values()[i]) }

	private val variantCyclers = Array(PhaseType.values().size) { i -> VariantCycler(i + (WIDTH * 3), PhaseType.values()[i]) }

	private val presetCycler = PresetCycler(coords(0, 4))
	private val killRewardCycler = KillRewardCycler(coords(1, 4))
	private val botToggle = BotToggle(coords(2, 4))
	private val defaultEnvironmentCycler = DefaultEnvironmentCycler(coords(3, 4))
	private val naturalRegenerationToggle = NaturalRegenerationToggle(coords(4, 4))

	private val worldButton = object : GuiItem(coords(6, 4)) {
		override fun onClick(player: Player, shift: Boolean) = WorldGenOption.worldGenGui.open(player)
		override fun getStack() = ItemCreator.fromType(Material.GOLD_ORE).name("${ChatColor.GREEN}World Gen Options").create()
	}

	private val resetButton = object : GuiItem(coords(7, 4)) {
		override fun onClick(player: Player, shift: Boolean) {
			UHC.defaultVariants.forEach { variant ->
				UHC.updateVariant(variant)
			}

			QuirkType.values().forEach { quirkType ->
				val quirk = UHC.getQuirk(quirkType)

				quirk.enabled.reset()
				quirk.resetProperties()
			}

			UHC.properties.forEach { it.reset() }
		}

		override fun getStack() = ItemCreator.fromType(Material.MUSIC_DISC_MALL).name("${ChatColor.AQUA}Reset").create()
	}

	private val cancelButton: GuiItem = CloseButton(coords(8, 4))

	init {
		quirkToggles.forEach { addItem(it) }
		variantCyclers.forEach { addItem(it) }

		addItem(presetCycler)
		addItem(killRewardCycler)
		addItem(botToggle)
		addItem(defaultEnvironmentCycler)
		addItem(naturalRegenerationToggle)
		addItem(worldButton)
		addItem(resetButton)
		addItem(cancelButton)
	}
}
