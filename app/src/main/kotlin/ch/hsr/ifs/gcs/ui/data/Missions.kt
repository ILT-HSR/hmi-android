package ch.hsr.ifs.gcs.ui.data

import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Scheduler
import ch.hsr.ifs.gcs.support.color.createRandomColorArgb
import org.osmdroid.views.overlay.Overlay

object Missions : Scheduler.OnSchedulerDataChangedListener {

    private val fItems = mutableListOf<Item>()

    val size: Int
        get() = fItems.size

    init {
        Scheduler.addListener(this)
    }

    class Item(val mission: Mission, val color: Int) {

        val status: String
            get() = mission.status

        var isSelected = false

        val mapOverlays: Collection<Overlay> = emptyList()

    }

    override fun onNewMissionAvailable(mission: Mission) {
        fItems += Item(mission, createRandomColorArgb())
    }

    override fun onMissionRemoved(mission: Mission) {
        fItems.removeIf {
            it.mission == mission
        }
    }

    fun forEach(block: (Item) -> Unit) {
        fItems.forEach(block)
    }

    operator fun get(index: Int) = fItems[index]

    fun isNotEmpty() = fItems.isNotEmpty()

    fun indexOf(item: Item) = fItems.indexOf(item)

}