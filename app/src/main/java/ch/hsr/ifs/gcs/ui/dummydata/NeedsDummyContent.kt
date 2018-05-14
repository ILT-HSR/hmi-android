package ch.hsr.ifs.gcs.ui.dummydata

import ch.hsr.ifs.gcs.model.*
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
        val taskList = ArrayList<Task<Any>>()
        taskList.add(ChooseTargetTask() as Task<Any>)
        taskList.add(ChooseCargoTask() as Task<Any>)
        addItem(NeedDummyItem(0, "Call-in", taskList))

        val taskList2 = ArrayList<Task<Any>>()
        taskList2.add(ChooseRegionTask() as Task<Any>)
        taskList2.add(ChooseAltitudeTask() as Task<Any>)
        taskList2.add(ChooseModeTask() as Task<Any>)
        addItem(NeedDummyItem(1,"Radiation Map", taskList2))
    }

    private fun addItem(item: NeedDummyItem) {
        NEED_ITEMS.add(item)
        NEED_ITEM_MAP[item.id] = item
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class NeedDummyItem(val id: Int, val name: String, val taskList: List<Task<Any>>) {
        var isSelected: Boolean = false
        override fun toString(): String = name
    }

}
