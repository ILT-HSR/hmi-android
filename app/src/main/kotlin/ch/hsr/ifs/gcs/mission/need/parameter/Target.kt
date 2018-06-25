package ch.hsr.ifs.gcs.mission.need.parameter

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
 * This [Parameter] implementation is used to configure the target of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Target : Parameter<GeoPoint> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.target"

    override lateinit var result: GeoPoint

    override fun resultToString(): String {
        result.let {
            var latitude = "${it.latitude}"
            latitude = latitude.dropLast(latitude.length - 7)
            var longitude = "${it.longitude}"
            longitude = longitude.dropLast(longitude.length - 7)
            return "$latitude, $longitude"
        }
    }

}