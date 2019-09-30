package ch.hsr.ifs.gcs.support.osmdroid

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Point
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class CircleOverlay(val position: GeoPoint, val radiusInMeters: Double = 1.0, val fillColor: Int = Color.DKGRAY) : Overlay() {

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val painter = Paint().apply {
            isAntiAlias = true
            strokeWidth = 0.0f
            color = fillColor
            style = Style.FILL
        }

        val projection = mapView.projection
        val screenPoint = Point()
        projection.toPixels(position, screenPoint)
        val radius = radiusInPixels(mapView)
        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), radius, painter)

    }

    private fun radiusInPixels(mapView: MapView): Float {
        return mapView.projection.metersToPixels(radiusInMeters.toFloat(), position.latitude, mapView.zoomLevelDouble)
    }

}
