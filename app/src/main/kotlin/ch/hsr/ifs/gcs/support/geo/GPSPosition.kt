package ch.hsr.ifs.gcs.support.geo

import org.osmdroid.util.GeoPoint

/**
 * A simple value type to hold GPS floating point coordinates
 *
 * @param latitude The latitude of the point (degrees)
 * @param longitude The longitude of the point (degrees)
 * @param altitude The altitude of the point (meters)
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class GPSPosition(val latitude: Double, val longitude: Double, val altitude: Double) {

    companion object {
        const val EARTH_RADIUS_IN_METERS = 6378137
    }

    /**
     * Convert a #WGS89Position into a #GPSPosition
     *
     * @since 1.0.0
     */
    constructor(position: WGS89Position) : this(
            position.latitude.toFloat() / 1e7,
            position.longitude.toFloat() / 1e7,
            position.altitude.toFloat() / 1e3)

    fun distanceTo(other: GPSPosition): Double {
        val fromLatitude = Math.toRadians(latitude)
        val fromLongitude = Math.toRadians(longitude)
        val toLatitude = Math.toRadians(other.latitude)
        val toLongitude = Math.toRadians(other.longitude)

        return EARTH_RADIUS_IN_METERS * 2 * Math.asin(Math.min(1.0, Math.sqrt(
                Math.pow(Math.sin((toLatitude - fromLatitude) / 2.0), 2.0)
                        + Math.cos(fromLatitude)
                        * Math.cos(toLatitude)
                        * Math.pow(Math.sin((toLongitude - fromLongitude) / 2.0), 2.0)
        )))
    }

    fun positionAt(distance: Double, compassBearing: Double): GPSPosition {
        val angularDistance = distance / EARTH_RADIUS_IN_METERS
        val bearingInRadians = Math.toRadians(compassBearing)
        val fromLatitude = Math.toRadians(latitude)
        val fromLongitude = Math.toRadians(longitude)

        val newLatitude = Math.asin(Math.sin(fromLatitude)
                * Math.cos(angularDistance)
                + Math.cos(fromLatitude)
                * Math.sin(angularDistance)
                * Math.cos(bearingInRadians))
        val newLongitude = fromLongitude + Math.atan2(
                Math.sin(bearingInRadians)
                        * Math.sin(angularDistance)
                        * Math.cos(fromLatitude),
                Math.cos(angularDistance)
                        - Math.sin(fromLatitude)
                        * Math.sin(newLatitude)
        )
        return GPSPosition(Math.toDegrees(newLatitude), Math.toDegrees(newLongitude), altitude)
    }

    val geoPoint: GeoPoint get() = GeoPoint(latitude, longitude, altitude)

}