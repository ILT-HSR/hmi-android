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

    private val fFlightPlan by lazy { buildFlightplan() }

    override val id = "ch.hsr.ifs.gcs.mission.need.mapping" //TODO: Move mapping to need descriptor

    override val parameterList get() = listOf(fMapType, fRegion)

    override val tasks: List<Task>?
        get() {
            return listOf(
                    LimitTravelSpeed(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TRAVEL_SPEED, PREFERENCE_DEFAULT_MAPPING_TRAVEL_SPEED).toDouble()),
                    TakeOff(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TAKEOFF_ALTITUDE, PREFERENCE_DEFAULT_MAPPING_TAKEOFF_ALTITUDE))) +
                    MoveToPosition(getStartingPointAt(fPreferences.getInt(PREFERENCE_KEY_MAPPING_TAKEOFF_ALTITUDE, PREFERENCE_DEFAULT_MAPPING_TAKEOFF_ALTITUDE).toDouble())) +
                    fFlightPlan.subList(0, 1) + ToggleSensor() + fFlightPlan.subList(1, fFlightPlan.size) +
                    ToggleSensor() +
                    ReturnToHome()
        }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

    private fun getSurveyCornersAt(altitude: Double) = fRegion.result.map {
        assert(fRegion.result.size == 4)
        GPSPosition(it.latitude, it.longitude, altitude)
    }

    private fun getStartingPointAt(altitude: Double) = run {
        val corners = getSurveyCornersAt(altitude)
        val northWestCorner = corners.first()
        val southWestCorner = corners.last()
        val areaHeight = northWestCorner.distanceTo(southWestCorner)
        val remainder = areaHeight.rem(SCAN_CORRIDOR_WIDTH)

        northWestCorner.positionAt(remainder / 2, COMPASS_BEARING_SOUTH)
    }

    private fun buildFlightplan(): List<Task> {
        val altitude = fPreferences.getInt(PREFERENCE_KEY_MAPPING_SURVEY_ALTITUDE, PREFERENCE_DEFAULT_MAPPING_SURVEY_ALTITUDE).toDouble()
        val (northWest, northEast, southEast) = getSurveyCornersAt(altitude)

        val width = northWest.distanceTo(northEast)
        val realLines = floor(northEast.distanceTo(southEast) / SCAN_CORRIDOR_WIDTH).toInt()

        return (0 until realLines).fold(listOf(MoveToPosition(getStartingPointAt(altitude)))) { plan, step ->
            val currentPoint = plan.last()
            val rowEnd = MoveToPosition(if (step % 2 == 0) {
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