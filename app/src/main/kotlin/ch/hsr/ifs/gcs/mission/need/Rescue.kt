package ch.hsr.ifs.gcs.mission.need

import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.parameter.Cargo
import ch.hsr.ifs.gcs.mission.need.parameter.Target
import ch.hsr.ifs.gcs.mission.need.task.Task
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_MOVE
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource

/**
 * This [Need] implementation represents the need to rescue a person from a known location to an
 * other location
 *
 * @since 1.2.0
 * @author ILT Institute for Lab Automation and Mechatronics
 */
class Rescue private constructor(override val resource: Resource, private val fPickUp: Target, private val fTarget: Target) : Need {

    @Suppress("unused")
    constructor(resource: Resource) : this(resource, Target(), Target())

    override val id = "ch.hsr.ifs.gcs.mission.need.rescue" //TODO: Move mapping to need descriptor

    override val parameterList get() = listOf(fPickUp, fTarget)

    override val tasks: List<Task>?
        get() = listOf(
        )

                override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_MOVE, true)
        )

    override fun copy() =
            Rescue(resource, fPickUp.copy(), fTarget.copy())


}