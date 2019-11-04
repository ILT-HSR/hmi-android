package ch.hsr.ifs.gcs.support.osmdroid

import android.graphics.*
import android.graphics.Paint.Style
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class CircleOverlay(val positions: List<GeoPoint>, val radiusInMeters: Double = 1.0, val colors: List<Int> = listOf(Color.DKGRAY)) : Overlay() {

    companion object {
        private fun radiusInPixels(radiusInMeters: Double, position: GeoPoint, mapView: MapView): Float {
            return mapView.projection.metersToPixels(radiusInMeters.toFloat(), position.latitude, mapView.zoomLevelDouble)
        }
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        val projection = mapView.projection
        val painter = Paint().apply {
            isAntiAlias = true
            strokeWidth = 0.0f
            style = Style.FILL
        }
        positions.zip(colors)
                .forEach { (pos, col) ->
                    painter.color = col
                    val screenPoint = Point()
                    projection.toPixels(pos, screenPoint)
                    val radius = radiusInPixels(radiusInMeters, pos, mapView)
                    canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), radius, painter)
                }
    }

}
