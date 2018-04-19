package ch.hsr.ifs.gcs.ui.dummy

import android.graphics.Color
import ch.hsr.ifs.gcs.ui.fragments.MissionResultsFragment
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.util.GeoPoint
import java.util.*


/**
 * Helper class for providing sample mission content for the [MissionResultsFragment].
 */
object MissionResultsDummyContent {

    /**
     * An array of sample (dummy) mission result items.
     */
    val MISSION_RESULT_ITEMS: MutableList<MissionResultDummyItem> = ArrayList()

    /**
     * A map of sample (dummy) mission result items, by ID.
     */
    private val MISSION_RESULT_ITEM_MAP: MutableMap<String, MissionResultDummyItem> = HashMap()

    private const val COUNT = 9

    init {
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: MissionResultDummyItem) {
        MISSION_RESULT_ITEMS.add(item)
        MISSION_RESULT_ITEM_MAP[item.id] = item
    }

    private fun createDummyItem(position: Int): MissionResultDummyItem {
        val color = createRandomColorArgb()
        return MissionResultDummyItem(position.toString(), createPolygons(color), makeDetails(position), color)
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0 until position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A dummy mission result item.
     */
    data class MissionResultDummyItem(val id: String, val mapOverlays: List<Polygon>, val details: String, val color: Int) {
        var isSelected: Boolean = false
        override fun toString(): String = id
    }

    private fun createPolygons(colorArgb: Int) : List<Polygon> {
        val polygonList = ArrayList<Polygon>()
        for (i in 1 until 5) {
            polygonList.add(createPolygon(colorArgb))
        }
        return polygonList
    }

    private fun createPolygon(colorArgb: Int) : Polygon {
        val random = Random()
        fun rand(from: Double, to: Double) : Double {
            return from + (to - from) * random.nextDouble()
        }
        val geoPoints = ArrayList<GeoPoint>()
        val latitude = rand(47.222, 47.224)
        val longitude = rand(8.814, 8.819)
        geoPoints.add(GeoPoint(latitude, longitude))
        geoPoints.add(GeoPoint(latitude-0.00007, longitude))
        geoPoints.add(GeoPoint(latitude-0.00007,longitude-0.0001))
        geoPoints.add(GeoPoint(latitude, longitude-0.0001))
        val polygon = Polygon()    //see note below
        polygon.fillColor = colorArgb
        polygon.strokeWidth = 2.5F
        geoPoints.add(geoPoints[0])    //forces the loop to close
        polygon.points = geoPoints
        polygon.title = "A sample polygon"
        return polygon
    }

    private fun createRandomColorArgb() : Int {
        val random = Random()
        fun rand(from: Int, to: Int) : Int {
            return random.nextInt(to - from) + from
        }
        val color = Color.argb(255, rand(0, 255), rand(0, 255), rand(0, 255))
        return color
    }

}
