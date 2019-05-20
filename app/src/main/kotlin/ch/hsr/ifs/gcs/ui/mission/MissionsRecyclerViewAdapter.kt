package ch.hsr.ifs.gcs.ui.mission


import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_missions.view.*
import kotlin.properties.Delegates


class MissionsRecyclerViewAdapter(private val fContext: MainActivity)
    : RecyclerView.Adapter<MissionsRecyclerViewAdapter.ViewHolder>() {

    private var fSelectedMission: MissionItem? = null
    private var fItems: List<MissionItem> = emptyList()
    private val fActiveItemColor = fContext.resources.getColor(R.color.activeListItem, null)

    var missions: List<Mission> by Delegates.observable(emptyList()) { _, old, new ->
        if (old != new) {
            fItems = new.map { MissionItem(it, fContext) }
            fSelectedMission?.let { act ->
                fItems.find { it.mission == act.mission }
            } ?: fItems.firstOrNull()?.let {
                select(it)
            }
            notifyDataSetChanged()
        }
    }

    val selection get() = fSelectedMission

    inner class ViewHolder(private val fView: View) : RecyclerView.ViewHolder(fView), Mission.Listener {
        private val fMissionSubType: ImageView = fView.mission_subtype
        private val fMissionName: TextView = fView.mission_name
        private val fMissionStatus: ImageView = fView.mission_status

        var item by Delegates.observable<MissionItem?>(null) { _, old, new ->
            when (new) {
                null -> Unit
                else -> {
                    fView.tag = new
                    fView.setOnClickListener { v ->
                        val item = v.tag as MissionItem
                        select(item)
                    }
                    fView.setBackgroundColor(if (fSelectedMission == new) fActiveItemColor else Color.TRANSPARENT)
                    val needItem = fContext.needItemFactory.instantiate(new.mission.need)
                    fMissionSubType.setImageDrawable(fContext.getDrawable(new.icon))
                    fMissionName.text = needItem.name
                    fMissionStatus.setImageDrawable(statusIconForMission(new))
                }
            }

            if(old != new) {
                new?.mission?.addListener(this)
                old?.mission?.removeListener(this)
            }
        }

        private fun statusIconForMission(item: MissionItem): Drawable {
            return item.statusIcon
        }

        override fun onMissionStatusChanged(mission: Mission, status: Mission.Status) {
            if(mission == this.item!!.mission) {
                val handler = Handler(Looper.getMainLooper())
                handler.post{ fMissionStatus.setImageDrawable(statusIconForMission(this.item!!)) }
            }
        }
    }

    // RecyclerView.Adapter implementation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        fItems[position].let {
            holder.item = it
        }
    }

    override fun getItemCount(): Int = missions.size

    // Private implementation

    private fun select(item: MissionItem) {
        fSelectedMission = item
        Log.i("MSRVA", "thread: ${Thread.currentThread().name}")
        item.draw()
        notifyDataSetChanged()
    }

}
