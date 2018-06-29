package ch.hsr.ifs.gcs.mission.access

import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.RadiationMap
import ch.hsr.ifs.gcs.resource.*
import ch.hsr.ifs.gcs.resource.ResourceManager
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_MOVE
import kotlin.reflect.full.primaryConstructor

class NeedProvider(private val fResourceManager: ResourceManager) {

    interface Listener {

        fun onNeedsAvailabilityChanged(availability: Boolean)

    }

    private val listeners = mutableListOf<Listener>()

    private val knownNeeds = kotlin.collections.mutableMapOf(
            "ch.hsr.ifs.gcs.mission.need.callIn" to Pair(CallIn::class, listOf(Capability(CAPABILITY_CAN_MOVE, true))),
            "ch.hsr.ifs.gcs.mission.need.radiationMap" to Pair(RadiationMap::class, listOf(Capability(CAPABILITY_CAN_FLY, true)))
    )

    private fun instantiate(id: String, resource: Resource) =
            knownNeeds[id]?.first?.primaryConstructor?.call(resource)

    val needs: List<Need>
        get() = emptyList()
//            knownNeeds.mapNotNull {
//                fResourceManager.get(*it.value.second.toTypedArray())?.let { res ->
//                    instantiate(it.key, res)
//                }
//            }.toList()

    fun addListener(listener: Listener) {
        listeners += listener
    }

    fun removeListener(listener: Listener) {
        listeners -= listener
    }

//    override fun onResourceAvailabilityChanged() {
//        val availability = !needs.isEmpty()
//        listeners.forEach{
//            it.onNeedsAvailabilityChanged(availability)
//        }
//    }
}