package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R.id.map
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener

class ChooseTargetTask : Task<GeoPoint> {

    override val name get() = "Target"

    override val description get() = "Choose a single target on the map."

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
        }
    }

    override fun cleanup(context: MainActivity) {}

}