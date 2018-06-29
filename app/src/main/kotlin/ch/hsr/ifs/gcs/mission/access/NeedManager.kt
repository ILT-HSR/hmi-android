package ch.hsr.ifs.gcs.mission.access

import android.arch.lifecycle.Observer
import ch.hsr.ifs.gcs.ResourceModel
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.mission.need.RadiationMap
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_MOVE
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import kotlin.reflect.full.primaryConstructor

class NeedManager(private val fListener: Listener) {

    interface Listener {
        fun onNewNeedAvailable(need: Need)
    }

    private val fNeedMap = kotlin.collections.mutableMapOf(
            "ch.hsr.ifs.gcs.mission.need.callIn" to Pair(CallIn::class, listOf<Capability<*>>(Capability(CAPABILITY_CAN_MOVE, true))),
            "ch.hsr.ifs.gcs.mission.need.radiationMap" to Pair(RadiationMap::class, listOf<Capability<*>>(Capability(CAPABILITY_CAN_FLY, true)))
    )
    private val fAvailableNeeds = mutableListOf<Need>()
    private val fResourceObserver = Observer<List<Resource>>{
        if (it != null) {
            val nonAvailableNeeds = fNeedMap.filter { d -> fAvailableNeeds.none { n -> n.id == d.key }}
            nonAvailableNeeds.forEach{id, des ->
                it.find { r -> des.second.all(r::has)}?.let{
                    with(instantiate(id, it)) {
                        fAvailableNeeds += this
                        fListener.onNewNeedAvailable(this)
                    }
                }
            }
        }
    }

    fun onCreate(resourceModel: ResourceModel) {
        resourceModel.availableResources.observeForever(fResourceObserver)
    }

    fun onDestroy(resourceModel: ResourceModel) {
        resourceModel.availableResources.removeObserver(fResourceObserver)
    }

    private fun instantiate(id: String, resource: Resource) =
            fNeedMap[id]!!.first.primaryConstructor!!.call(resource)

}