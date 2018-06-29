package ch.hsr.ifs.gcs.mission.access

import android.arch.lifecycle.Observer
import android.util.Log
import ch.hsr.ifs.gcs.ResourceModel
import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.mission.need.RadiationMap
import ch.hsr.ifs.gcs.resource.*
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_MOVE
import kotlin.reflect.full.primaryConstructor

class NeedManager(private val fResourceModel: ResourceModel) {

    companion object {
        private const val LOG_TAG = "NeedManager"
    }

    private val fNeedMap = kotlin.collections.mutableMapOf(
            "ch.hsr.ifs.gcs.mission.need.callIn" to Pair(CallIn::class, listOf(Capability(CAPABILITY_CAN_MOVE, true))),
            "ch.hsr.ifs.gcs.mission.need.radiationMap" to Pair(RadiationMap::class, listOf(Capability(CAPABILITY_CAN_FLY, true)))
    )

    init {
        fResourceModel.availableResources.observeForever(Observer {
            if(it != null) {
                Log.i(LOG_TAG, "Available resources changed: $it")
            }
        })
    }

    private fun instantiate(id: String, resource: Resource) =
            fNeedMap[id]?.first?.primaryConstructor?.call(resource)

}