package ch.hsr.ifs.gcs.ui.fragments.needs

import ch.hsr.ifs.gcs.needs.CallInNeed
import ch.hsr.ifs.gcs.needs.Need
import ch.hsr.ifs.gcs.needs.RadiationMapNeed
import ch.hsr.ifs.gcs.resources.*
import kotlin.reflect.full.primaryConstructor

object NeedsManager {

    private val knownNeeds = kotlin.collections.mutableMapOf(
            "ch.hsr.ifs.gcs.need.callIn" to Pair(CallInNeed::class, listOf(Capability(CAPABILITY_CAN_MOVE, true))),
            "ch.hsr.ifs.gcs.need.radiationMap" to Pair(RadiationMapNeed::class, listOf(Capability(CAPABILITY_CAN_FLY, true)))
    )

    private fun instantiate(id: String, resource: Resource) =
            knownNeeds[id]?.first?.primaryConstructor?.call(resource)

    val needs: List<Need>
        get() =
            knownNeeds.mapNotNull {
                ResourceManager.get(*it.value.second.toTypedArray())?.let { res ->
                    NeedsManager.instantiate(it.key, res)
                }
            }.toList()
}