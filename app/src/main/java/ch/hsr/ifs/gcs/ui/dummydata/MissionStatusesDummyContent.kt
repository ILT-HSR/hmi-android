package ch.hsr.ifs.gcs.ui.dummydata

import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample mission content for the [MissionStatusesFragment].
 */
object MissionStatusesDummyContent {

    /**
     * An array of sample (dummy) mission status items.
     */
    val MISSION_STATUS_ITEMS: MutableList<DummyItem> = ArrayList()

    /**
     * A map of sample (dummy) mission status items, by ID.
     */
    private val MISSION_STATUS_ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

    private const val COUNT = 25

    init {
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: DummyItem) {
        MISSION_STATUS_ITEMS.add(item)
        MISSION_STATUS_ITEM_MAP[item.id] = item
    }

    private fun createDummyItem(position: Int): DummyItem {
        return DummyItem(position.toString(), "Mission", makeDetails(position))
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
     * A dummy mission status item.
     */
    data class DummyItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }

}
