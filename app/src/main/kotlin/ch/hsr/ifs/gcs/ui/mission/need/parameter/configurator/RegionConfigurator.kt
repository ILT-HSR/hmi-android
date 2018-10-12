package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Suppress("unused")
class RegionConfigurator : ParameterConfigurator<List<GeoPoint>>() {

    data class RectangularRegion(private val upperLeft: GeoPoint, private val lowerRight: GeoPoint) {

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

    private lateinit var region: RectangularRegion

    override fun present() {
        val mapView = context.findViewById<MapView>(R.id.map)

        val polygon = createInitialPolygon(mapView)
        parameter.parameter.result = region.getPolygonPoints()
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
                    parameter.parameter.result = region.getPolygonPoints()
                    mapView.invalidate()
                }
            })
        }
        mapView.invalidate()
    }

    override fun destroy() {
        context.map.overlays.forEach {
            if (it is Marker) {
                it.isDraggable = false
                it.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
            }
        }
    }

    override fun abort() {
        context.map.overlays.clear()
        context.map.invalidate()
    }

    private fun createInitialPolygon(mapView: MapView): Polygon {
        val zoomLevel = mapView.zoomLevelDouble
        val currentGeoPoint = mapView.mapCenter
        val currentLatitude = currentGeoPoint.latitude
        val currentLongitude = currentGeoPoint.longitude
        val latitudeDiff = (0.00007 / 2) * zoomLevel
        val longitudeDiff = (0.0001 / 2) * zoomLevel
        val polygon = Polygon()
        region = RectangularRegion(
                GeoPoint(currentLatitude + latitudeDiff, currentLongitude - longitudeDiff),
                GeoPoint(currentLatitude - latitudeDiff, currentLongitude + longitudeDiff)
        )
        val pointList = region.getPolygonPoints()
        polygon.points = pointList
        return polygon
    }

}
