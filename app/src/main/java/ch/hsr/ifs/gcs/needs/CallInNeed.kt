package ch.hsr.ifs.gcs.needs

import android.location.Location
import ch.hsr.ifs.gcs.needs.parameters.CargoNeedParameter
import ch.hsr.ifs.gcs.needs.parameters.NeedParameter
import ch.hsr.ifs.gcs.needs.parameters.TargetNeedParameter
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_MOVE
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource
import ch.hsr.ifs.gcs.task.MoveToPosition
import ch.hsr.ifs.gcs.task.Task
import ch.hsr.ifs.gcs.task.TriggerPayload

/**
 * This [Need] implementation represents the need to drop a cargo at a chosen location.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CallInNeed(override val resource: Resource) : Need {

    private val targetParameter = TargetNeedParameter()
    private val cargoParameter = CargoNeedParameter()

    override val name = "Call-in"

    override val needParameterList: List<NeedParameter<*>> = arrayListOf(
            targetParameter,
            cargoParameter)

    override var isActive = false

    override fun getTasks(): List<Task>? {
        if(targetParameter.isCompleted && cargoParameter.isCompleted) {
            val location = Location("")
            location.latitude = targetParameter.result!!.latitude
            location.longitude = targetParameter.result!!.longitude
            val moveToTask = MoveToPosition(location)
            val triggerPayloadTask = TriggerPayload(cargoParameter.result!!)
            return arrayListOf(moveToTask, triggerPayloadTask)
        } else {
            return null
        }
    }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_MOVE, true)
        )

}