package ch.hsr.ifs.gcs.ui.data

import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Scheduler
import ch.hsr.ifs.gcs.support.color.createRandomColorArgb
import org.osmdroid.views.overlay.Overlay

object Missions : Scheduler.OnSchedulerDataChangedListener {

    interface Listener {

        fun onItemAdded(index: Int)

        fun onItemRemoved(index: Int)

        fun onItemUpdated(index: Int)

    }

    class Item(val color: Int) {

        var status: String = ""

        var isSelected = false

        val mapOverlays: Collection<Overlay> = emptyList()

    }

    private val fItems = mutableMapOf<Mission, Item>()
    private val fListeners = mutableListOf<Listener>()

    val size get() = fItems.size

    init {
        Scheduler.addListener(this)
    }

    fun addListener(listener: Listener) {
        fListeners += listener
    }

    fun removeListener(listener: Listener) {
        fListeners -= listener
    }

    override fun onNewMissionAvailable(mission: Mission) {
        with(Item(createRandomColorArgb())) {
            fItems[mission] = this
            fListeners.forEach { it.onItemAdded(indexOf(this)) }
        }
    }

    override fun onMissionRemoved(mission: Mission) {
        val index = fItems.keys.indexOf(mission)
        fItems.remove(mission)
        fListeners.forEach { it.onItemRemoved(index) }
    }

    override fun onMissionStatusChanged(mission: Mission) {
        fItems[mission]?.apply {
            status = mission.status
            fListeners.forEach { it.onItemUpdated(indexOf(this)) }
        }
    }

    fun forEach(block: (Item) -> Unit) {
        fItems.values.forEach(block)
    }

    operator fun get(index: Int) = fItems.values.elementAt(index)

    fun isNotEmpty() = fItems.isNotEmpty()

    fun indexOf(item: Item) = fItems.values.indexOf(item)

}