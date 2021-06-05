package com.codeland.uhc.discord.filesystem

import java.util.*
import kotlin.collections.ArrayList

class LinkDataFile(header: String, channelName: String) : DiscordFile <LinkDataFile.Companion.LinkData> (header, channelName) {
	companion object {
		data class LinkData(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val discordIds: ArrayList<Long> = ArrayList()
		)
	}

	override fun fromContents(contents: String): LinkData {
		val lines = contents.lines()

		val minecraftIds = ArrayList<UUID>()
		val discordIds = ArrayList<Long>()

		lines.forEach { line ->
			val parts = line.split(',')

			if (parts.size == 2) {
				try {
					val uuid = UUID.fromString(parts[0])
					val discordID = parts[1].toLong()

					minecraftIds.add(uuid)
					discordIds.add(discordID)

				} catch (ex: Exception) {}
			}
		}

		return LinkData(minecraftIds, discordIds)
	}

	override fun writeContents(data: LinkData): String {
		return data.minecraftIds.indices.joinToString("\n") { i ->
			"${data.minecraftIds[i]},${data.discordIds[i]}"
		}
	}

	override fun defaultContents(): String {
		return "MINECRAFT_UUID,DISCORD_ID\nMINECRAFT_UUID,DISCORD_ID\n..."
	}
}
