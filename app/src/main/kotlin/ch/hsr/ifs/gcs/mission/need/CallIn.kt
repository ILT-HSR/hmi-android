package ch.hsr.ifs.gcs.mission.need

import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.PREFERENCE_KEY_CALL_IN_ALTITUDE
import ch.hsr.ifs.gcs.PREFERENCE_KEY_CALL_IN_TRAVEL_SPEED
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.parameter.Cargo
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

    private val fPreferences = PreferenceManager.getDefaultSharedPreferences(GCS.context)

    private var fTarget = Target()
    private var fCargo = Cargo()

    override val id = "ch.hsr.ifs.gcs.mission.need.callIn" //TODO: Move mapping to need descriptor

    override var parameterList = listOf(
            fCargo,
            fTarget
    )

    override val tasks: List<Task>?
        get() = listOf(
                LimitTravelSpeed(fPreferences.getInt(PREFERENCE_KEY_CALL_IN_TRAVEL_SPEED, 1).toDouble()),
                TakeOff(fPreferences.getInt(PREFERENCE_KEY_CALL_IN_ALTITUDE, 1)),
                MoveToPosition(GPSPosition(fTarget.result.latitude, fTarget.result.longitude, fPreferences.getInt(PREFERENCE_KEY_CALL_IN_ALTITUDE, 1).toDouble())),
                TriggerPayload(fCargo.result),
                ReturnToHome()
        )

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

    override fun copy(): CallIn {
        val copy = CallIn(resource)
        copy.fCargo = fCargo.copy() as Cargo
        copy.fTarget = fTarget.copy() as Target
        copy.parameterList = listOf(
                copy.fCargo, copy.fTarget
        )
        return copy
    }

}