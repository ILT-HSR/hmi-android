package ch.hsr.ifs.gcs.mission.need

import android.location.Location
import ch.hsr.ifs.gcs.mission.need.parameter.Cargo
import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.mission.need.parameter.Target
import ch.hsr.ifs.gcs.mission.need.task.MoveToPosition
import ch.hsr.ifs.gcs.mission.need.task.ReturnToHome
import ch.hsr.ifs.gcs.mission.need.task.Task
import ch.hsr.ifs.gcs.mission.need.task.TriggerPayload
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_MOVE

/**
 * This [Need] implementation represents the need to drop a cargo at a chosen location.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CallIn(override val resource: Resource) : Need {

    private val target = Target()
    private val cargo = Cargo()

    override val name = "Call-in"

    override val parameterList = listOf(target, cargo)

    override var isActive = false

    override val tasks: List<Task>?
        get() =
            if (parameterList.all(Parameter<*>::isCompleted)) {
                val location = Location("")
                location.latitude = target.result!!.latitude
                location.longitude = target.result!!.longitude
                listOf(MoveToPosition(location), TriggerPayload(cargo.result!!), ReturnToHome())
            } else {
                null
            }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_MOVE, true)
        )

}