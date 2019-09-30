package ch.hsr.ifs.gcs.mission.need

import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.*
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.parameter.MapType
import ch.hsr.ifs.gcs.mission.need.parameter.Region
import ch.hsr.ifs.gcs.mission.need.task.*
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import kotlin.math.IEEErem
import kotlin.math.floor

/**
 * This [Need] implementation represents the need to generate a heat map for radiation in a
 * provided region using a certain mode.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Mapping private constructor(override val resource: Resource, private val fMapType: MapType, private val fRegion: Region) : Need {

    @Suppress("unused")
    constructor(resource: Resource) : this(resource, MapType(), Region())

    @Deprecated("Should live in configuration")
    private companion object {
        const val SCAN_CORRIDOR_WIDTH = 2.0
        const val COMPASS_BEARING_EAST = 90.0
        const val COMPASS_BEARING_SOUTH = 180.0
        const val COMPASS_BEARING_WEST = 270.0
    }

    private val fPreferences = PreferenceManager.getDefaultSharedPreferences(GCS.context)

    override val id = "ch.hsr.ifs.gcs.mission.need.mapping" //TODO: Move mapping to need descriptor

    override val parameterList get() = listOf(fMapType, fRegion)

    override val tasks: List<Task>?
        get() {
            return listOf(
                    LimitTravelSpeed(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TRAVEL_SPEED, PREFERENCE_DEFAULT_MAPPING_TRAVEL_SPEED).toDouble()),
                    TakeOff(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TAKEOFF_ALTITUDE, PREFERENCE_DEFAULT_MAPPING_TAKEOFF_ALTITUDE))) +
                    ToggleSensor() +
                    buildFlightplan() +
                    ToggleSensor() +
                    ReturnToHome()
        }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

    private fun buildFlightplan(): List<Task> {
        assert(fRegion.result.size == 4)

        val altitude = fPreferences.getInt(PREFERENCE_KEY_MAPPING_SURVEY_ALTITUDE, PREFERENCE_DEFAULT_MAPPING_SURVEY_ALTITUDE).toDouble()

        val corners = fRegion.result
        val (northWest, northEast, southEast) = corners.map { GPSPosition(it.latitude, it.longitude, altitude) }

        val width = northWest.distanceTo(northEast)
        val height = northEast.distanceTo(southEast)

        val remainder = height.IEEErem(SCAN_CORRIDOR_WIDTH)
        val realLines = floor(height / SCAN_CORRIDOR_WIDTH).toInt()

        val startPoint = northWest.positionAt(remainder / 2 * SCAN_CORRIDOR_WIDTH, COMPASS_BEARING_SOUTH)

        return (0 until realLines).fold(listOf(MoveToPosition(startPoint))) { plan, step ->
            val currentPoint = plan.last()
            val rowEnd = MoveToPosition(if(step % 2 == 0) {
                currentPoint.targetLocation.positionAt(width, COMPASS_BEARING_EAST)
            } else {
                currentPoint.targetLocation.positionAt(width, COMPASS_BEARING_WEST)
            })
            plan + rowEnd + MoveToPosition(rowEnd.targetLocation.positionAt(SCAN_CORRIDOR_WIDTH, COMPASS_BEARING_SOUTH))
        }
    }

    override fun copy() =
            Mapping(resource, fMapType.copy(), fRegion.copy())

}