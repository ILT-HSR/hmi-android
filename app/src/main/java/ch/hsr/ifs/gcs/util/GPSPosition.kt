package ch.hsr.ifs.gcs.util

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

    /**
     * Convert a #WGS89Position into a #GPSPosition
     *
     * @since 1.0.0
     */
    constructor(position: WGS89Position) : this(
            position.latitude.toFloat() / 1e7,
            position.longitude.toFloat() / 1e7,
            position.altitude.toFloat() / 1e3)

}