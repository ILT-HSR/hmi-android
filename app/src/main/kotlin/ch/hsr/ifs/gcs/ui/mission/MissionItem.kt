package ch.hsr.ifs.gcs.ui.mission

import android.graphics.drawable.Drawable
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.ui.MainActivity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MissionItem(val mission: Mission, val context: MainActivity) {

    var icon: Int = 0
    val statusIcon: Drawable?
        get() {
            return when(mission.status) {
                Mission.Status.PREPARING, Mission.Status.ACTIVE -> context.getDrawable(R.drawable.checkbox_active_incomplete)
                Mission.Status.FINISHED -> context.getDrawable(R.drawable.checkbox_active_complete)
                Mission.Status.ABORTED, Mission.Status.FAILED -> context.getDrawable(R.drawable.checkbox_aborted)
            }
        }
    private var color: Int = 0
    private var targets: MutableList<GPSPosition> = emptyList<GPSPosition>().toMutableList()
    private var regions: MutableList<List<GPSPosition>> = emptyList<List<GPSPosition>>().toMutableList()

    init {
        mission.need.parameterList.forEach {
            when (val result = it.result) {
                is String -> {
                    when (result) {
                        "Medkit" -> icon = R.drawable.ic_medkit_location_marker
                        "Radiation" -> {
                            icon = R.drawable.ic_radiation_maptype_marker
                            color = context.getColor(R.color.radiationMapTransparent)
                        }
                    }
                }
                is GPSPosition -> targets.add(result)
                is List<*> -> regions.add(result as List<GPSPosition>)
            }
        }
    }

    fun draw() {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.overlays.forEach { overlay ->
            if(overlay !is MyLocationNewOverlay) {
                mapView.overlays.remove(overlay)
            }
        }
        if (targets.isNotEmpty()) {
            targets.forEach {
                val targetMarker = Marker(mapView)
                targetMarker.position = GeoPoint(it.latitude, it.longitude)
                targetMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                targetMarker.icon = context.getDrawable(icon)
                mapView.overlays.add(targetMarker)
            }
        }
        if (regions.isNotEmpty()) {
            regions.forEach {
                val polygon = Polygon().apply {
                    points = it.map {
                        GeoPoint(it.latitude, it.longitude)
                    }
                    fillColor = color
                    strokeColor = color
                    strokeWidth = 0.0f
                }
                mapView.overlays.add(polygon)
                val mapTypeMarker = Marker(mapView)
                mapTypeMarker.position = GeoPoint(polygon.points[0])
                mapTypeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapTypeMarker.icon = context.getDrawable(icon)
                mapView.overlays.add(mapTypeMarker)
            }
        }
        mapView.invalidate()
    }

}