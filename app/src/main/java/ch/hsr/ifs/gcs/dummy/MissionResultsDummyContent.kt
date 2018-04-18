package ch.hsr.ifs.gcs.dummy

import ch.hsr.ifs.gcs.MissionResultsFragment
import java.util.ArrayList
import java.util.HashMap

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

    private const val COUNT = 25

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
        return MissionResultDummyItem(position.toString(), "Resultat", makeDetails(position))
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
    data class MissionResultDummyItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }

}
