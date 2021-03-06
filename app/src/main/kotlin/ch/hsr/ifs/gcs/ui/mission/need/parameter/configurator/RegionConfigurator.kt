package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import ch.hsr.ifs.gcs.GCS
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

    data class RectangularRegion(private var upperLeft: GeoPoint, private var lowerRight: GeoPoint, private val map: MapView) {

        private val dragPointMap = mapOf(
                "region_marker_UL" to this::upperLeft,
                "region_marker_LR" to this::lowerRight
        )

        val polygon: Polygon
            get() = Polygon().apply {
                points = listOf(
                        upperLeft,
                        GeoPoint(upperLeft.latitude, lowerRight.longitude),
                        lowerRight,
                        GeoPoint(lowerRight.latitude, upperLeft.longitude)
                )
                id = "region_polygon"
                fillColor = GCS.context.resources.getColor(R.color.selectionTransparent, null)
                strokeColor = GCS.context.resources.getColor(R.color.selectionTransparent, null)
                strokeWidth = 0.0f
            }

        val dragPoints: List<Marker>
            get() =
                dragPointMap.map { (mid, field) ->
                    Marker(map).apply {
                        id = mid
                        position = field.get()
                        isDraggable = true
                        setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                            override fun onMarkerDragEnd(marker: Marker?) {}
                            override fun onMarkerDragStart(marker: Marker) = onMarkerDrag(marker)
                            override fun onMarkerDrag(marker: Marker) {
                                field.set(marker.position)
                                map.overlays.asSequence()
                                        .filter { it is Polygon && it.id == "region_polygon" }
                                        .map { it as Polygon }.toList()
                                        .forEach { map.overlays -= it }
                                map.overlays += polygon
                                map.invalidate()
                            }
                        })
                    }
                }

    }

    private lateinit var region: RectangularRegion

    override fun present() {
        super.present()
        val map = context.map
        region = createInitialRegion(map)
        showInstructionText(GCS.context.getString(R.string.region_instruction))
        map.overlays.add(region.polygon)
        region.dragPoints.forEach { marker ->
            marker.icon = context.getDrawable(R.drawable.ic_location_marker)
            map.overlays += marker
        }

        map.invalidate()
    }

    override fun destroy() {
        val map = context.map
        map.overlays.forEach {
            if (it is Marker) {
                map.overlayManager.remove(it)
            }
        }
        hideInstructionText()
        parameter.parameter.result = region.polygon.points.map {
            GPSPosition(it.latitude, it.longitude, it.altitude)
        }
        super.destroy()
    }

    override fun abort() {
        context.map.overlays.clear()
        context.map.invalidate()
        super.abort()
    }

    private fun createInitialRegion(mapView: MapView): RectangularRegion {
        val currentGeoPoint = mapView.mapCenter
        val currentLatitude = currentGeoPoint.latitude
        val currentLongitude = currentGeoPoint.longitude

        val height = mapView.projection.boundingBox.longitudeSpan
        val width = mapView.projection.boundingBox.latitudeSpan

        return RectangularRegion(
                GeoPoint(currentLatitude + width / 2 * 0.75, currentLongitude - height / 2 * 0.75),
                GeoPoint(currentLatitude - width / 2 * 0.75, currentLongitude + height / 2 * 0.75),
                mapView
        )
    }

}
