package ch.hsr.ifs.gcs.ui.fragments.needs

import ch.hsr.ifs.gcs.needs.CallInNeed
import ch.hsr.ifs.gcs.needs.Need
import ch.hsr.ifs.gcs.needs.RadiationMapNeed
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_MOVE
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object NeedsManager {

    val needs: MutableMap<String, Pair<KClass<out Need>, List<Capability<*>>>>

    init {
        needs = mutableMapOf(
                "ch.hsr.ifs.gcs.need.callIn" to Pair(CallInNeed::class, listOf(Capability(CAPABILITY_CAN_MOVE, true))),
                "ch.hsr.ifs.gcs.need.radiationMap" to Pair(RadiationMapNeed::class, listOf(Capability(CAPABILITY_CAN_FLY, true)))
        )
    }

    fun instantiate(id: String, resource: Resource) =
            needs[id]?.first?.primaryConstructor?.call(resource)
}