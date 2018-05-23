package ch.hsr.ifs.gcs.needs.parameters

import android.graphics.Canvas
import android.view.MotionEvent
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R.id.map
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener
import org.osmdroid.views.overlay.Overlay

/**
 * This [NeedParameter] implementation is used to configure the target of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class TargetNeedParameter : NeedParameter<GeoPoint> {

    override val name = "Target"

    override val description = "Choose a single target on the map."

    override var result: GeoPoint? = null

    override fun resultToString(): String {
        result?.let {
            var latitude = "${it.latitude}"
            latitude = latitude.dropLast(latitude.length - 7)
            var longitude = "${it.longitude}"
            longitude = longitude.dropLast(longitude.length - 7)
            return "$latitude, $longitude"
        }
        return ""
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        context.locationService?.getCurrentLocation()?.let {
            result = GeoPoint(it)
            val mapView = context.findViewById<MapView>(map)
            val posMarker = Marker(mapView)
            posMarker.position = GeoPoint(it)
            posMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(posMarker)
            mapView.invalidate()
            posMarker.isDraggable = true
            posMarker.setOnMarkerDragListener(object : OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {}
                override fun onMarkerDragEnd(marker: Marker) {
                    result = GeoPoint(marker.position)
                }
                override fun onMarkerDrag(marker: Marker) {}
            })
            mapView.overlays.add(object : Overlay(context) {
                override fun draw(c: Canvas?, osmv: MapView?, shadow: Boolean) {}
                override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                    val proj = mapView.projection
                    val geoPoint = proj.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    posMarker.position = geoPoint
                    mapView.invalidate()
                    result = geoPoint
                    return true
                }
            })

        }
    }

    override fun cleanup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(map)
        mapView.overlays.removeAt(mapView.overlays.size - 1)
        val posMarker = mapView.overlays[0] as Marker
        posMarker.isDraggable = false
        posMarker.setOnMarkerClickListener { _, _ -> true } // needed to prevent info box pop up
    }

}