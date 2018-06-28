package ch.hsr.ifs.gcs.ui.mission.need

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.ui.BasicHardwareControllable
import ch.hsr.ifs.gcs.ui.HardwareControllable
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.MenuFragmentID
import ch.hsr.ifs.gcs.ui.mission.need.NeedsFragment.OnNeedsFragmentChangedListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need.view.*
import java.lang.IllegalStateException
import kotlin.properties.Delegates

/**
 * [RecyclerView.Adapter] that can display a [Need] and makes a call to the
 * specified [OnNeedsFragmentChangedListener].
 */
class NeedsRecyclerViewAdapter(
        private val fListener: OnNeedsFragmentChangedListener?,
        private val fRecyclerView: RecyclerView,
        private val fContext: MainActivity)
    :
        RecyclerView.Adapter<NeedsRecyclerViewAdapter.ViewHolder>(),
        Input.Listener,
        HardwareControllable<NeedsRecyclerViewAdapter> by BasicHardwareControllable(fContext.inputProvider) {

    private var fActiveItem: NeedItem? = null
    private var fItems: List<NeedItem> = emptyList()
    private val fOnClickListener = View.OnClickListener { v ->
        val item = v.tag as NeedItem
        activateItem(item)
        disableHardwareControls(this)
        fListener?.onNeedItemChanged(item)
    }
    private val fActiveItemColor = fContext.resources.getColor(R.color.activeListItem, null)

    var needs: List<Need> by Delegates.observable(emptyList()) { _, old, new ->
        if (old != new) {
            fItems = new.map(fContext.needItemFactory::instantiate)
            fActiveItem?.let { act ->
                fItems.find { it.need == act.need }?.activate()
            } ?: fItems.firstOrNull()?.let {
                it.activate()
                fActiveItem = it
            }
            notifyDataSetChanged()
        }
    }

    val activeItem get() = fActiveItem ?: throw IllegalStateException("No need is selected")

    inner class ViewHolder(private val fView: View) : RecyclerView.ViewHolder(fView) {
        private val fNameView: TextView = fView.need_name

        var item by Delegates.observable<NeedItem?>(null) { _, _, new ->
            when (new) {
                null -> Unit
                else -> {
                    fView.tag = new
                    fView.setOnClickListener(fOnClickListener)
                    fView.setBackgroundColor(if (new.isActive) fActiveItemColor else Color.TRANSPARENT)
                    fNameView.text = new.name
                }
            }
        }
    }

    init {
        enableHardwareControls(this)
    }

    // RecyclerView.Adapter implementation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_need, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       fItems[position].let {
            holder.item = it
        }
    }

    override fun getItemCount(): Int = needs.size

    // Input.Listener implementation

    override fun onButton(control: Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Control.DPAD_UP -> {
                activatePreviousItem()
            }
            Control.DPAD_DOWN -> {
                activateNextItem()
            }
            Control.UPDATE_ABORT -> {
                fContext.showMenuFragment(MenuFragmentID.MISSION_STATUSES_FRAGMENT)
                fContext.leftButton.background = fContext.getDrawable(R.drawable.abort_mission)
                disableHardwareControls(this)
            }
            Control.NEED_START -> {
                fListener?.onNeedItemChanged(fActiveItem!!)
                disableHardwareControls(this)
            }
        }
    }

    private fun activateNextItem() {
        val newIndex = fItems.indexOf(fActiveItem) + 1
        if (newIndex < fItems.size) {
            activateItem(fItems[newIndex])
        }
    }

    private fun activatePreviousItem() {
        val newIndex = fItems.indexOf(fActiveItem) - 1
        if (newIndex >= 0) {
            activateItem(fItems[newIndex])
        }
    }

    private fun activateItem(item: NeedItem) {
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
