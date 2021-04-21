package com.codeland.uhc.phase

import com.codeland.uhc.phase.phases.endgame.*
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.postgame.PostgameDefault
import com.codeland.uhc.phase.phases.shrink.ShrinkDefault
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import org.bukkit.Material

enum class PhaseVariant(var type: PhaseType, var createPhase: () -> Phase, var prettyName: String, var representation: Material, var description: List<String>) {
	WAITING_DEFAULT(PhaseType.WAITING, ::WaitingDefault, "Default", Material.TARGET, listOf(
		"Everyone waits for the UHC to start"
	)),

	GRACE_DEFAULT(PhaseType.GRACE, ::GraceDefault, "Default", Material.OXEYE_DAISY, listOf(
		"You may regenerate health and are free from combat to get you started",
	)),

	SHRINK_DEFAULT(PhaseType.SHRINK, ::ShrinkDefault, "Default", Material.SPAWNER, listOf(
		"The border begins to force you to the center"
	)),

	ENDGAME_NATURAL_TERRAIN(PhaseType.ENDGAME, ::EndgameNaturalTerrain, "Natural Terrain", Material.OAK_SAPLING, listOf(
		"Underground disappears, as well as the sky",
		"Players are forced to fight on the surface"
	)),

	ENDGAME_GLOWING(PhaseType.ENDGAME, ::EndgameGlowingAll, "Glowing", Material.GLOWSTONE_DUST, listOf(
		"Glowing is applied to everyone",
		"Now you cannot hide"
	)),

	POSTGAME_DEFAULT(PhaseType.POSTGAME, ::PostgameDefault, "Default", Material.FILLED_MAP, listOf(
		"Hooray, the match is over!"
	));

	fun start(time: Int, onInject: (Phase) -> Unit): Phase {
		val ret = createPhase()

		ret.start(type, this, time, onInject)

		return ret
	}
}
