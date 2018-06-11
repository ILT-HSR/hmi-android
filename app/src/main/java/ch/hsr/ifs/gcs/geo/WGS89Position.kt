package ch.hsr.ifs.gcs.geo

/**
 * A simple value type to hold scaled WGS89 integer coordinates
 *
 * @param latitude The latitude of the point (degrees * 10e7)
 * @param longitude The longitude of the point (degrees * 10e7)
 * @param altitude The altitude of the point (meters * 10e3)
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class WGS89Position(val latitude: Int, val longitude: Int, val altitude: Int) {

    /**
     * Convert a #GPSPosition into a #WGS89Position
     *
     * @since 1.0.0
     */
    constructor(position: GPSPosition) : this(
            (position.latitude * 1e7).toInt(),
            (position.longitude * 1e7).toInt(),
            (position.altitude * 1e3).toInt())
}