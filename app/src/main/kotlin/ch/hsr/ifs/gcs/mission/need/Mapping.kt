package ch.hsr.ifs.gcs.mission.need

import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.PREFERENCE_KEY_MAPPING_ALTITUDE
import ch.hsr.ifs.gcs.PREFERENCE_KEY_MAPPING_TRAVEL_SPEED
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.need.parameter.MapType
import ch.hsr.ifs.gcs.mission.need.parameter.Region
import ch.hsr.ifs.gcs.mission.need.task.*
import ch.hsr.ifs.gcs.resource.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ilt.uxv.hmi.core.support.geo.GPSPosition
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
                    LimitTravelSpeed(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TRAVEL_SPEED, 1).toDouble()),
                    TakeOff(fPreferences.getInt(PREFERENCE_KEY_MAPPING_ALTITUDE, 1))) + buildFlightplan() + ReturnToHome()
        }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

    private fun buildFlightplan(): List<Task> {
        val corners = fRegion.result
        val topLeft = GPSPosition(corners[0].latitude, corners[0].longitude, fPreferences.getInt(PREFERENCE_KEY_MAPPING_ALTITUDE, 1).toDouble())
        val topRight = GPSPosition(corners[1].latitude, corners[1].longitude, fPreferences.getInt(PREFERENCE_KEY_MAPPING_ALTITUDE, 1).toDouble())
        val bottomLeft = GPSPosition(corners[3].latitude, corners[3].longitude, fPreferences.getInt(PREFERENCE_KEY_MAPPING_ALTITUDE, 1).toDouble())

        val width = topLeft.distanceTo(topRight)
        val height = topLeft.distanceTo(bottomLeft)
        val realLines = floor(height / SCAN_CORRIDOR_WIDTH).toInt()
        val remainder = height.IEEErem(SCAN_CORRIDOR_WIDTH)

        var currentPoint = topLeft.positionAt(remainder / 2 * SCAN_CORRIDOR_WIDTH, COMPASS_BEARING_SOUTH)
        val flightPlan = mutableListOf<Task>(MoveToPosition(currentPoint))
        return (0 until realLines).map {
            val result = if ((it % 2) == 0) {
                currentPoint.positionAt(width, COMPASS_BEARING_EAST)
            } else {
                currentPoint.positionAt(width, COMPASS_BEARING_WEST)
            }
            currentPoint = result.positionAt(SCAN_CORRIDOR_WIDTH, COMPASS_BEARING_SOUTH)
            listOf(
                    MoveToPosition(result),
                    MoveToPosition(currentPoint)
            )
        }.flatten().toCollection(flightPlan)
    }

    override fun copy() =
            Mapping(resource, fMapType.copy(), fRegion.copy())

}