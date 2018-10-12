package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Suppress("unused")
class RegionConfigurator : ParameterConfigurator<List<GPSPosition>>() {

    override fun present() {
        val mapView = context.findViewById<MapView>(R.id.map)

        val polygon = createInitialPolygon(mapView)
        mapView.overlays.add(polygon)
        polygon.points.forEach {
            Marker(mapView).apply {
                id = "region_marker"
                isDraggable = true
                position = it
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays += this
            }
        }

        mapView.invalidate()
    }

    override fun destroy() {
        val map = context.map
        map.overlays.forEach {
            if (it is Marker) {
                it.isDraggable = false
                it.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
            }
        }

        parameter.parameter.result = map.overlays
                .filter { it is Marker }
                .map { it as Marker }
                .filter { it.id == "region_marker" }
                .map {
                    GPSPosition(
                            it.position.latitude,
                            it.position.longitude,
                            Double.NaN
                    )
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

        val upperLeft = GeoPoint(currentLatitude + latitudeDiff, currentLongitude - longitudeDiff)
        val lowerRight = GeoPoint(currentLatitude - latitudeDiff, currentLongitude + longitudeDiff)
        val polygon = Polygon()
        polygon.points = listOf(
                GeoPoint(upperLeft.latitude, upperLeft.longitude),
                GeoPoint(upperLeft.latitude, lowerRight.longitude),
                GeoPoint(lowerRight.latitude, lowerRight.longitude),
                GeoPoint(lowerRight.latitude, upperLeft.longitude)
        )
        return polygon
    }

}
