package ch.hsr.ifs.gcs.need.parameter

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.*

/**
 * This [Parameter] implementation is used to define a region in which a need has to take place.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Region : Parameter<List<GeoPoint>> {

    private lateinit var region: Region

    override val name = "Region"

    override val description = "Choose the region your mission should be carried out in."

    override var result: List<GeoPoint>? = ArrayList()

    override fun resultToString(): String {
        var string = ""
        result?.let {
            string = "${it.size} Waypoints"
        }
        return string
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)

        val polygon = createInitialPolygon(mapView)
        result = region.getPolygonPoints()
        mapView.overlays.add(polygon)
        region.getRegionPoints().forEach {
            val marker = Marker(mapView)
            marker.isDraggable = true
            marker.position = it
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
            marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {}
                override fun onMarkerDrag(marker: Marker) {
                    it.latitude = marker.position.latitude
                    it.longitude = marker.position.longitude
                    polygon.points = region.getPolygonPoints()
                    result = region.getPolygonPoints()
                    mapView.invalidate()
                }
            })
        }
        mapView.invalidate()
    }

    private fun createInitialPolygon(mapView: MapView): Polygon {
        val zoomLevel = mapView.zoomLevelDouble
        val currentGeoPoint = mapView.mapCenter
        val currentLatitude = currentGeoPoint.latitude
        val currentLongitude = currentGeoPoint.longitude
        val latitudeDiff = (0.00007 / 2) * zoomLevel
        val longitudeDiff = (0.0001 / 2) * zoomLevel
        val polygon = Polygon()
        region = Region(
                GeoPoint(currentLatitude + latitudeDiff, currentLongitude - longitudeDiff),
                GeoPoint(currentLatitude - latitudeDiff, currentLongitude + longitudeDiff)
        )
        val pointList = region.getPolygonPoints()
        polygon.points = pointList
        return polygon
    }

    override fun cleanup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.overlays.forEach {
            if (it is Marker) {
                it.isDraggable = false
                it.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
            }
        }
    }

    data class Region(private val upperLeft: GeoPoint, private val lowerRight: GeoPoint) {

        fun getPolygonPoints() = listOf(
                upperLeft,
                GeoPoint(upperLeft.latitude, lowerRight.longitude),
                lowerRight,
                GeoPoint(lowerRight.latitude, upperLeft.longitude)
        )

        fun getRegionPoints() = listOf(
                upperLeft,
                lowerRight
        )

    }

}