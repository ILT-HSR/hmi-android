package ch.hsr.ifs.gcs.ui.dummydata

import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample mission content for the [NeedsFragment].
 */
object NeedsDummyContent {

    /**
     * An array of sample (dummy) need items.
     */
    val NEED_ITEMS: MutableList<NeedDummyItem> = ArrayList()

    /**
     * A map of sample (dummy) need items, by ID.
     */
    private val NEED_ITEM_MAP: MutableMap<String, NeedDummyItem> = HashMap()

    private const val COUNT = 12

    init {
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }

    private fun addItem(item: NeedDummyItem) {
        NEED_ITEMS.add(item)
        NEED_ITEM_MAP[item.id] = item
    }

    private fun createDummyItem(position: Int): NeedDummyItem {
        return NeedDummyItem(position.toString(), "Bedürfnis", makeDetails(position))
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
     * A dummy item representing a piece of content.
     */
    data class NeedDummyItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }

}
