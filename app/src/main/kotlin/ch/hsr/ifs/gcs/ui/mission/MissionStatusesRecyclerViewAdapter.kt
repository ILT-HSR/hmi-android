package ch.hsr.ifs.gcs.ui.mission


import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Mission
import kotlinx.android.synthetic.main.fragment_missionstatuses.view.*
import kotlin.properties.Delegates

class MissionStatusesRecyclerViewAdapter(private val fRecyclerView: RecyclerView)
    : RecyclerView.Adapter<MissionStatusesRecyclerViewAdapter.ViewHolder>() {

    private var fActiveItem: MissionItem? = null
    private var fItems: List<MissionItem> = emptyList()
    private val fOnClickListener = View.OnClickListener { v ->
        val item = v.tag as MissionItem
        activateItem(item)
    }
    private val fActiveItemColor = fRecyclerView.resources.getColor(R.color.activeListItem, null)

    var missions: List<Mission> by Delegates.observable(emptyList()) { _, old, new ->
        if (old != new) {
            fItems = new.map(::MissionItem)
            fActiveItem?.let { act ->
                fItems.find { it.mission == act.mission }?.activate()
            } ?: fItems.firstOrNull()?.let {
                it.activate()
                fActiveItem = it
            }
            notifyDataSetChanged()
        }
    }

    val activeItem get() = fActiveItem ?: throw IllegalArgumentException("No mission is selected")

    inner class ViewHolder(private val fView: View) : RecyclerView.ViewHolder(fView) {
        private val fMissionName: TextView = fView.mission_name

        var item by Delegates.observable<MissionItem?>(null) { _, _, new ->
            when (new) {
                null -> Unit
                else -> {
                    fView.tag = new
                    fView.setOnClickListener(fOnClickListener)
                    fView.setBackgroundColor(if (new.isActive) fActiveItemColor else Color.TRANSPARENT)
                    fMissionName.text = new.status
                }
            }
        }
    }

    fun activateNextItem() {
        val newIndex = fItems.indexOf(fActiveItem) + 1
        if (newIndex < fItems.size) {
            activateItem(fItems[newIndex])
        }
    }

    fun activatePreviousItem() {
        val newIndex = fItems.indexOf(fActiveItem) + 1
        if (newIndex >= 0) {
            activateItem(fItems[newIndex])
        }
    }

    // RecyclerView.Adapter implementation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionstatuses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = fItems[position]
    }

    override fun getItemCount(): Int = missions.size

    // Private implementation

    private fun activateItem(item: MissionItem) {
        fActiveItem?.let {
            it.deactivate()
            val index = fItems.indexOf(it)
            (fRecyclerView.findViewHolderForLayoutPosition(index) as ViewHolder).item = it
        }
        item.let {
            it.activate()
            fActiveItem = it
            val index = fItems.indexOf(it)
            (fRecyclerView.findViewHolderForLayoutPosition(index) as ViewHolder).item = it
        }
    }

}
