package ch.hsr.ifs.gcs.mission.need

import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.parameter.Altitude
import ch.hsr.ifs.gcs.mission.need.parameter.Cargo
import ch.hsr.ifs.gcs.mission.need.parameter.SpeedLimit
import ch.hsr.ifs.gcs.mission.need.parameter.Target
import ch.hsr.ifs.gcs.mission.need.task.*
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.support.geo.GPSPosition

/**
 * This [Need] implementation represents the need to drop a cargo at a chosen location.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CallIn(override val resource: Resource) : Need {

    private val fAltitude = Altitude()
    private val fTarget = Target()
    private val fCargo = Cargo()
    private val fSpeedLimit = SpeedLimit()

    override val id = "ch.hsr.ifs.gcs.mission.need.callIn"

    override val parameterList = listOf(
            fAltitude,
            fSpeedLimit,
            fTarget,
            fCargo
    )

    override val tasks: List<Task>?
        get() = listOf(
                LimitTravelSpeed(fSpeedLimit.result),
                TakeOff(fAltitude.result),
                MoveToPosition(GPSPosition(fTarget.result.latitude, fTarget.result.longitude, fAltitude.result.toDouble())),
                TriggerPayload(fCargo.result),
                ReturnToHome()
        )

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

}