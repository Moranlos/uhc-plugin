package com.codeland.uhc.world.chunkPlacer

import org.bukkit.Chunk

abstract class ImmediateChunkPlacer(size: Int) : AbstractChunkPlacer(size) {
	override fun onGenerate(chunk: Chunk, seed: Int) {
		val chunkIndex = shouldGenerate(chunk.x, chunk.z, seed, uniqueSeed, size)

		if (chunkIndex != -1) place(chunk, chunkIndex)
	}
}
