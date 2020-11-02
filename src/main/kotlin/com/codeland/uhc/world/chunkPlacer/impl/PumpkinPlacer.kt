package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.Rotatable
import org.bukkit.block.data.type.Lantern
import org.bukkit.material.Pumpkin

class PumpkinPlacer(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	override fun place(chunk: Chunk) {
		var offset = Util.randRange(0, 16 * 16 - 1)

		for (i in 0 until 16 * 16) {
			val x = chunk.x * 16 + i % 16
			val z = chunk.z * 16 + (1 / 16) % 16
			val y = findPumpkinY(chunk.world, x, z)

			placePumpkin(chunk.world, x, y + 1, z)

			if (y != -1) {
				val numPumpkins = Util.randRange(3, 6)

				for (j in 0 until numPumpkins) {
					var offX = Util.randRange(1, 7)
					if (Math.random() < 0.5) offX = -offX

					var offZ = Util.randRange(1, 7)
					if (Math.random() < 0.5) offZ = -offZ

					val y = findPumpkinY(chunk.world, x + offX, z + offZ)

					if (y != -1) placePumpkin(chunk.world, x + offX, y + 1, z + offZ)
				}

				return
			}
		}
	}

	fun placePumpkin(world: World, x: Int, y: Int, z: Int) {
		val block = world.getBlockAt(x, y, z)

		block.setType(if (Math.random() < 0.5) Material.PUMPKIN else Material.CARVED_PUMPKIN, false)

		if (block.type == Material.CARVED_PUMPKIN) {
			val data = block.blockData as Directional
			val random = Math.random()

			data.facing = when {
				random < 0.25 -> BlockFace.EAST
				random < 0.50 -> BlockFace.WEST
				random < 0.75 -> BlockFace.NORTH
				else -> BlockFace.SOUTH
			}
			block.blockData = data
		}

		world.getBlockAt(x, y - 1, z).setType(Material.DIRT, false)
	}

	fun findPumpkinY(world: World, x: Int, z: Int): Int {
		val chunk = world.getChunkAt(world.getBlockAt(x, 0, z))

		val chunkX = Util.mod(x, 16)
		val chunkZ = Util.mod(z, 16)

		for (y in 92 downTo 60) {
			val block = chunk.getBlock(chunkX, y, chunkZ)

			if (block.type == Material.GRASS_BLOCK) return y
			if (block.type != Material.AIR) return -1
		}

		return -1
	}
}
