package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.CallIn
import ch.hsr.ifs.gcs.mission.need.Need

object NeedItemFactory {

    private val NEED_ITEM_CONSTRUCTORS = mutableMapOf<String, (Need) -> NeedItem>(
            "ch.hsr.ifs.gcs.mission.need.callIn" to { n -> CallInItem(n as CallIn) }
    )

    fun register(id: String, constructor: (Need) -> NeedItem) {
        if(NEED_ITEM_CONSTRUCTORS.contains(id)) {
            throw IllegalArgumentException("Constructor for type '$id' is already registered")
        }

        NEED_ITEM_CONSTRUCTORS[id] = constructor
    }

    fun instantiate(id: String, need: Need) = if(NEED_ITEM_CONSTRUCTORS.contains(id)) {
        NEED_ITEM_CONSTRUCTORS[id]!!.invoke(need)
    } else {
        throw IllegalArgumentException("No constructor for $id")
    }

}