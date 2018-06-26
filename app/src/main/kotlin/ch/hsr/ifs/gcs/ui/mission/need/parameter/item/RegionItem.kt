package ch.hsr.ifs.gcs.ui.mission.need.parameter.item

import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.need.parameter.Region
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class RegionItem(parameter: Region) : BasicParameterItem<List<GeoPoint>>(parameter) {

    private lateinit var region: LocalRegion

    override val name = "Region"

    override fun setup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)

        val polygon = createInitialPolygon(mapView)
        parameter.result = region.getPolygonPoints()
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
                    parameter.result = region.getPolygonPoints()
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
        region = LocalRegion(
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

    data class LocalRegion(private val upperLeft: GeoPoint, private val lowerRight: GeoPoint) {

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