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
    private val NEED_ITEM_MAP: MutableMap<Int, NeedDummyItem> = HashMap()

    init {
        val taskList = ArrayList<Task>()
        taskList.add(Task("target", "Choose Target"))
        taskList.add(Task("cargo", "Choose Cargo"))
        taskList.add(Task("start", "Start Mission"))
        addItem(NeedDummyItem(0, "Call In", taskList))

        val taskList2 = ArrayList<Task>()
        taskList2.add(Task("area", "Choose Target Area"))
        taskList2.add(Task("start", "Start Mission"))
        addItem(NeedDummyItem(1,"Heatmap", taskList2))
    }

    private fun addItem(item: NeedDummyItem) {
        NEED_ITEMS.add(item)
        NEED_ITEM_MAP[item.id] = item
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class NeedDummyItem(val id: Int, val name: String, val taskList: List<Task>) {
        override fun toString(): String = name
    }

    /**
     * A dummy task representing a task step of a need.
     */
    data class Task(val name: String, val description: String) {
        override fun toString(): String = name
    }

}
