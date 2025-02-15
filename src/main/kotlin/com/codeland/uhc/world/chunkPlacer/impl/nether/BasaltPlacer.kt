package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.random.Random

class BasaltPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyAround(world, chunkX, chunkZ)
	}

	fun border(block: Block): Boolean {
		return block.type === Material.LAVA || block.type === Material.AIR
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		val random = Random(chunk.chunkKey.xor(chunk.world.seed))

		val centerX = random.nextInt(16)
		val centerY = random.nextInt(6, 24)
		val centerZ = random.nextInt(16)

		val radius = random.nextInt(4, 8)

		for (x in centerX - radius..centerX + radius) {
			for (y in centerY - radius..centerY + radius) {
				for (z in centerZ - radius..centerZ + radius) {
					val block = chunk.world.getBlockAt(chunk.x * 16 + x, y, chunk.z * 16 + z)

					if (
						(block.type === Material.BLACKSTONE || block.type === Material.BASALT) &&
						(x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) + (z - centerZ) * (z - centerZ) <= radius * radius
					) {
						block.setType(Material.SMOOTH_BASALT, false)
					}
				}
			}
		}
	}
}
