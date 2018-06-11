package ch.hsr.ifs.gcs.need

import android.location.Location
import ch.hsr.ifs.gcs.need.parameter.Cargo
import ch.hsr.ifs.gcs.need.parameter.Parameter
import ch.hsr.ifs.gcs.need.parameter.Target
import ch.hsr.ifs.gcs.need.task.MoveToPosition
import ch.hsr.ifs.gcs.need.task.Task
import ch.hsr.ifs.gcs.need.task.TriggerPayload
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_MOVE
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource

/**
 * This [Need] implementation represents the need to drop a cargo at a chosen location.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CallIn(override val resource: Resource) : Need {

    private val targetParameter = Target()
    private val cargoParameter = Cargo()

    override val name = "Call-in"

    override val parameterList: List<Parameter<*>> = arrayListOf(
            targetParameter,
            cargoParameter)

    override var isActive = false

    override val tasks: List<Task>?
        get() {
            if (targetParameter.isCompleted && cargoParameter.isCompleted) {
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