package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.SugarCanePlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder

class SugarCaneFix : ChunkPlacerHolder() {
	companion object {
		val deepSugarCanePlacer = SugarCanePlacer(2, 58, 62)
		val lowSugarCanePlacer = SugarCanePlacer(6, 63, 63)
		val highSugarCanePlacer = SugarCanePlacer(4, 64, 82)
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		deepSugarCanePlacer,
		lowSugarCanePlacer,
		highSugarCanePlacer
	)
}