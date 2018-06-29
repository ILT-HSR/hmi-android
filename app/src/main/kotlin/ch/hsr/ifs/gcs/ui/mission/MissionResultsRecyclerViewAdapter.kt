package ch.hsr.ifs.gcs.ui.mission


import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Result
import kotlinx.android.synthetic.main.fragment_missionresults.view.*
import kotlin.properties.Delegates

class MissionResultsRecyclerViewAdapter(private val fRecyclerView: RecyclerView)
    : RecyclerView.Adapter<MissionResultsRecyclerViewAdapter.ViewHolder>() {

    private var fActiveItem: ResultItem? = null
    private var fItems: List<ResultItem> = emptyList()
    private val fOnClickListener = View.OnClickListener { v ->
        val item = v.tag as ResultItem
        activateItem(item)
    }
    private val fActiveItemColor = fRecyclerView.resources.getColor(R.color.activeListItem, null)

    var results: List<Result> by Delegates.observable(emptyList()) { _, old, new ->
        if(old != new) {
            fItems = new.map(::ResultItem)
            fActiveItem?.let { act ->
                fItems.find { it.result == act.result }?.activate()
            } ?: fItems.firstOrNull()?.let {
                it.activate()
                fActiveItem = it
            }
            notifyDataSetChanged()
        }
    }

    val activeItem get() = fActiveItem ?: throw IllegalStateException("No result is selected")

    inner class ViewHolder(private val fView: View) : RecyclerView.ViewHolder(fView) {
        private val fMissionName: TextView = fView.mission_name

        var item by Delegates.observable<ResultItem?>(null) { _, _, new ->
            when (new) {
                null -> Unit
                else -> {
                    fView.tag = new
                    fView.setOnClickListener(fOnClickListener)
                    fView.setBackgroundColor(if (new.isActive) fActiveItemColor else Color.TRANSPARENT)
                    fMissionName.text = "Result"
                }
            }
        }
    }

    fun activateNextItem() {
        val newIdex = fItems.indexOf(fActiveItem) + 1
        if(newIdex < fItems.size) {
            activateItem(fItems[newIdex])
        }
    }

    fun activatePreviousItem() {
        val newIdex = fItems.indexOf(fActiveItem) - 1
        if(newIdex >= fItems.size) {
            activateItem(fItems[newIdex])
        }
    }

    // RecyclerView.Adapter implementation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionresults, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = fItems[position]
    }

    override fun getItemCount(): Int = fItems.size

    // Private implementation

    private fun activateItem(item: ResultItem) {
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
