package ch.hsr.ifs.gcs.mission.access

import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.mission.need.RadiationMap
import ch.hsr.ifs.gcs.resource.*
import ch.hsr.ifs.gcs.resource.access.ResourceManager
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_MOVE
import kotlin.reflect.full.primaryConstructor

class NeedProvider(private val fResourceManager: ResourceManager) : ResourceManager.Listener {

    interface OnNeedsAvailabilityChangedListener {

        fun onNeedsAvailabilityChanged(availability: Boolean)

    }

    private val listeners = mutableListOf<OnNeedsAvailabilityChangedListener>()

    private val knownNeeds = kotlin.collections.mutableMapOf(
            "ch.hsr.ifs.gcs.mission.need.callIn" to Pair(CallIn::class, listOf(Capability(CAPABILITY_CAN_MOVE, true))),
            "ch.hsr.ifs.gcs.mission.need.radiationMap" to Pair(RadiationMap::class, listOf(Capability(CAPABILITY_CAN_FLY, true)))
    )

    init {
        fResourceManager.addListener(this)
    }

    private fun instantiate(id: String, resource: Resource) =
            knownNeeds[id]?.first?.primaryConstructor?.call(resource)

    val needs: List<Need>
        get() =
            knownNeeds.mapNotNull {
                fResourceManager.get(*it.value.second.toTypedArray())?.let { res ->
                    instantiate(it.key, res)
                }
            }.toList()

    fun addListener(listener: OnNeedsAvailabilityChangedListener) {
        listeners += listener
    }

    fun removeListener(listener: OnNeedsAvailabilityChangedListener) {
        listeners -= listener
    }

    override fun onResourceAvailabilityChanged() {
        val availability = !needs.isEmpty()
        listeners.forEach{
            it.onNeedsAvailabilityChanged(availability)
        }
    }
}