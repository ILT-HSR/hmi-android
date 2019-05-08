package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.graphics.Canvas
import android.view.MotionEvent
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@Suppress("unused")
class TargetConfigurator : ParameterConfigurator<GPSPosition>() {

    override fun present() {
        val mapView = context.findViewById<MapView>(R.id.map)
        val mapCenter = mapView.mapCenter as GeoPoint
        parameter.parameter.result = GPSPosition(mapCenter.latitude, mapCenter.longitude, mapCenter.altitude)
        val posMarker = Marker(mapView)
        posMarker.position = mapView.mapCenter as GeoPoint?
        posMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        posMarker.icon = context.getDrawable(R.drawable.ic_location_marker)
        mapView.overlays.add(posMarker)
        showInstructionText(GCS.context.getString(R.string.target_instruction))
        mapView.invalidate()
        posMarker.isDraggable = true
        posMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                val position = marker.position
                parameter.parameter.result = GPSPosition(position.latitude, position.longitude, position.altitude)
            }

            override fun onMarkerDrag(marker: Marker) {}
        })
        mapView.overlays.add(object : Overlay() {
            override fun draw(c: Canvas?, osmv: MapView?, shadow: Boolean) {}
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val geoPoint = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                posMarker.position = geoPoint
                mapView.invalidate()
                parameter.parameter.result = GPSPosition(geoPoint.latitude, geoPoint.longitude, geoPoint.altitude)
                return true
            }
        })
    }

    override fun destroy() {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.overlays.removeAt(mapView.overlays.size - 1)
        val posMarker = mapView.overlays[mapView.overlays.size - 1] as Marker
        posMarker.isDraggable = false
        posMarker.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
        hideInstructionText()
        mapView.invalidate()
    }

    override fun abort() {
        context.map.overlays.clear()
        context.map.invalidate()
    }
}
