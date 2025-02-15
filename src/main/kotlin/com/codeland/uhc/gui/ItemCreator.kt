package com.codeland.uhc.gui

import com.codeland.uhc.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

class ItemCreator private constructor(val type: Material, val meta: ItemMeta, clean: Boolean) {
	var amount = 1

	init {
		if (clean) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
		else meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
	}

	fun name(name: String) : ItemCreator {
		meta.displayName(noItalic(Component.text(name)))
		return this
	}

	fun name(component: Component) : ItemCreator {
		meta.displayName(noItalic(component))
		return this
	}

	fun lore(vararg lore: String) : ItemCreator {
		meta.lore(lore.map { str -> noItalic(Component.text(str)) })
		return this
	}

	fun lore(list: List<Component>) : ItemCreator {
		meta.lore(list)
		return this
	}

	fun lore(component: Component) : ItemCreator {
		meta.lore(listOf(noItalic(component)))
		return this
	}

	fun <M : ItemMeta> customMeta(edit: (M) -> Unit): ItemCreator {
		edit(meta as M)
		return this
	}

	fun enchant(): ItemCreator {
		meta.addEnchant(ItemUtil.fakeEnchantment, 0, true)
		return this
	}

	fun enchant(enchantment: Enchantment, level: Int = 1): ItemCreator {
		meta.addEnchant(enchantment, level, true)
		return this
	}

	fun enchant(enchant: Boolean): ItemCreator {
		if (enchant) meta.addEnchant(ItemUtil.fakeEnchantment, 0, true)
		return this
	}

	fun enchant(enchant: Pair<Enchantment, Int>): ItemCreator {
		meta.addEnchant(enchant.first, enchant.second, true)
		return this
	}

	fun setData(key: NamespacedKey, value: Int): ItemCreator {
		(meta as PersistentDataHolder).persistentDataContainer.set(key, PersistentDataType.INTEGER, value)
		return this
	}

	fun amount(value: Int): ItemCreator {
		amount = value
		return this
	}

	fun create(): ItemStack {
		val stack = ItemStack(type, amount)
		stack.itemMeta = meta
		return stack
	}

	fun modify(stack: ItemStack) {
		stack.type = type
		stack.amount = amount
		stack.itemMeta = meta
	}

	/* util */
	companion object {
		fun noItalic(component: Component): Component {
			return component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
		}

		fun stateName(base: String, state: String): TextComponent {
			return Component.text("${ChatColor.WHITE}$base ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}$state")
		}

		fun enabledName(base: String, enabled: Boolean): TextComponent {
			return Component.text("${ChatColor.WHITE}$base ${ChatColor.GRAY}- ${if (enabled) ChatColor.GREEN else ChatColor.RED}${ChatColor.BOLD}${if (enabled) "Enabled" else "Disabled"}")
		}

		fun getData(key: NamespacedKey, stack: ItemStack): Int? {
			return (stack.itemMeta as PersistentDataHolder).persistentDataContainer.get(key, PersistentDataType.INTEGER)
		}

		fun regular(type: Material): ItemCreator {
			return ItemCreator(type, Bukkit.getItemFactory().getItemMeta(type), false)
		}

		fun fromType(type: Material, clean: Boolean = true): ItemCreator {
			return ItemCreator(type, Bukkit.getItemFactory().getItemMeta(type), clean)
		}

		fun fromStack(stack: ItemStack, clean: Boolean = true): ItemCreator {
			return ItemCreator(stack.type, stack.itemMeta, clean).amount(stack.amount)
		}
	}
}