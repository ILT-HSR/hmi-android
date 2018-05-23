package ch.hsr.ifs.gcs.needs.parameters

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.*

/**
 * This [NeedParameter] implementation is used to define a region in which a need has to take place.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class RegionNeedParameter : NeedParameter<List<GeoPoint>> {

    override val name = "Region"

    override val description = "Choose the region your mission should be carried out in."

    override var result: List<GeoPoint>? = ArrayList()

    override fun resultToString(): String {
        TODO("not implemented")
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)
        val zoomLevel = mapView.zoomLevelDouble
        var polygon = createInitialPolygon(context, zoomLevel)
        result = polygon?.points
        mapView.overlays.add(polygon)
        polygon?.points?.forEach {
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
                    polygon.points = polygon.points.toList()
                    mapView.invalidate()
                }
            })
        }
        mapView.invalidate()
    }

    private fun createInitialPolygon(context: MainActivity, zoomLevel: Double): Polygon? {
        context.locationService?.getCurrentLocation()?.let {
            val currentGeoPoint = GeoPoint(it)
            val currentLatitude = currentGeoPoint.latitude
            val currentLongitude = currentGeoPoint.longitude
            val latitudeDiff = (0.00007 / 2) * zoomLevel
            val longitudeDiff = (0.0001 / 2) * zoomLevel
            val polygon = Polygon()
            val pointList = arrayListOf(
                    GeoPoint(currentLatitude - latitudeDiff, currentLongitude - longitudeDiff),
                    GeoPoint(currentLatitude - latitudeDiff, currentLongitude + longitudeDiff),
                    GeoPoint(currentLatitude + latitudeDiff, currentLongitude + longitudeDiff),
                    GeoPoint(currentLatitude + latitudeDiff, currentLongitude - longitudeDiff)
            )
            polygon.points = pointList
            return polygon
        }
        return null
    }

    override fun cleanup(context: MainActivity) {
        TODO("not implemented")
    }

}