package ch.hsr.ifs.gcs.ui.mission


import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Mission
import kotlinx.android.synthetic.main.fragment_missions.view.*
import kotlin.properties.Delegates

class MissionsRecyclerViewAdapter(view: RecyclerView)
    : RecyclerView.Adapter<MissionsRecyclerViewAdapter.ViewHolder>() {

    private var fSelectedMission: Mission? = null
    private val fActiveItemColor = view.resources.getColor(R.color.activeListItem, null)

    var missions: List<Mission> by Delegates.observable(emptyList()) { _, old, new ->
        if (old != new) {
            fSelectedMission = fSelectedMission?.let {
                if (it !in new) {
                    new.firstOrNull()
                } else {
                    it
                }
            } ?: new.firstOrNull()
            notifyDataSetChanged()
        }
    }

    val selection get() = fSelectedMission

    inner class ViewHolder(private val fView: View) : RecyclerView.ViewHolder(fView), Mission.Listener {
        private val fMissionName: TextView = fView.mission_name

        var mission by Delegates.observable<Mission?>(null) { _, old, new ->
            when (new) {
                null -> Unit
                else -> {
                    fView.tag = new
                    fView.setOnClickListener { v ->
                        val item = v.tag as Mission
                        select(item)
                    }
                    fView.setBackgroundColor(if (fSelectedMission == new) fActiveItemColor else Color.TRANSPARENT)
                    fMissionName.text = new.status.name
                }
            }

            if(old != new) {
                new?.addListener(this)
                old?.removeListener(this)
            }
        }

        override fun onMissionStatusChanged(mission: Mission, status: Mission.Status) {
            if(mission == this.mission) {
                val handler = Handler(Looper.getMainLooper())
                handler.post{ fMissionName.text = status.name }
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
        holder.mission = missions[position]
    }

    override fun getItemCount(): Int = missions.size

    // Private implementation

    private fun select(item: Mission) {
        fSelectedMission = item
        Log.i("MSRVA", "thread: ${Thread.currentThread().name}")
        notifyDataSetChanged()
    }


}
