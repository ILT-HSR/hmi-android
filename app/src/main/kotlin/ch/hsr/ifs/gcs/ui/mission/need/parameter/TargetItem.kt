package ch.hsr.ifs.gcs.ui.mission.need.parameter

import android.graphics.Canvas
import android.view.MotionEvent
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R.id.map
import ch.hsr.ifs.gcs.mission.need.parameter.Target
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class TargetItem(parameter: Target) : BasicParameterItem<GeoPoint>(parameter) {

    override val name = "Target"

    override fun setup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(map)
        parameter.result = mapView.mapCenter as GeoPoint
        val posMarker = Marker(mapView)
        posMarker.position = mapView.mapCenter as GeoPoint?
        posMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(posMarker)
        mapView.invalidate()
        posMarker.isDraggable = true
        posMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}
            override fun onMarkerDragEnd(marker: Marker) {
                parameter.result = GeoPoint(marker.position)
            }

            override fun onMarkerDrag(marker: Marker) {}
        })
        mapView.overlays.add(object : Overlay() {
            override fun draw(c: Canvas?, osmv: MapView?, shadow: Boolean) {}
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val proj = mapView.projection
                val geoPoint = proj.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                posMarker.position = geoPoint
                mapView.invalidate()
                parameter.result = geoPoint
                return true
            }
        })
    }

    override fun cleanup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(map)
        mapView.overlays.removeAt(mapView.overlays.size - 1)
        val posMarker = mapView.overlays[0] as Marker
        posMarker.isDraggable = false
        posMarker.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
    }

}