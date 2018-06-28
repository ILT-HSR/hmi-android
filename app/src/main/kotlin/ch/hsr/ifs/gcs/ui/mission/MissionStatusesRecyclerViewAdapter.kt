package ch.hsr.ifs.gcs.ui.mission


import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.access.MissionProvider
import ch.hsr.ifs.gcs.support.color.createRandomColorArgb
import ch.hsr.ifs.gcs.ui.BasicHardwareControllable
import ch.hsr.ifs.gcs.ui.HardwareControllable
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.fragments.MenuFragmentID
import ch.hsr.ifs.gcs.ui.mission.MissionStatusesFragment.OnStatusesFragmentChangedListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionstatuses.view.*

/**
 * [RecyclerView.Adapter] that can display a [MissionProvider.Item] and makes a call to the
 * specified [OnStatusesFragmentChangedListener].
 */
class MissionStatusesRecyclerViewAdapter(
        private val mListener: OnStatusesFragmentChangedListener?,
        private val mRecyclerView: RecyclerView,
        private val mContext: MainActivity)
    :
        RecyclerView.Adapter<MissionStatusesRecyclerViewAdapter.ViewHolder>(),
        Input.Listener,
        MissionProvider.Listener,
        HardwareControllable<MissionStatusesRecyclerViewAdapter> by BasicHardwareControllable(mContext.inputProvider){

    companion object {
        private val LOG_TAG = MissionStatusesRecyclerViewAdapter::class.java.simpleName
    }

    private val mOnClickListener: View.OnClickListener
    private val mMissionItems = mutableListOf<MissionItem>()
    private var activeItem: MissionItem? = null

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mMissionName: TextView = mView.mission_name
        override fun toString(): String {
            return super.toString() + " '" + mMissionName.text + "'"
        }
    }

    init {
        if (mMissionItems.isNotEmpty()) {
            activeItem = mMissionItems[0]
            activeItem!!.isSelected = true
        }

        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as MissionItem
            activateItem(item)
            mListener?.onStatusItemChanged(item)
        }

        enableHardwareControls(this)
        MissionProvider.addListener(this)
    }

    val items get() = mMissionItems.toList()

    // Adapter implementation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionstatuses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mMissionItems[position]
        holder.mView.setBackgroundColor(
                if (item.isSelected) {
                    Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
                } else {
                    Color.TRANSPARENT
                })
        holder.mMissionName.text = item.status
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mMissionItems.size

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
            Control.DPAD_RIGHT -> {
                mContext.showMenuFragment(MenuFragmentID.MISSION_RESULTS_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.refresh_mission)
                disableHardwareControls(this)
            }
            Control.NEED_START -> {
                mContext.showMenuFragment(MenuFragmentID.NEEDS_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.cancel_action)
                disableHardwareControls(this)
            }
            Control.UPDATE_ABORT -> {
                Log.d(LOG_TAG, "Cancel Mission Pressed")
            }
        }
    }

    // MissionProvider.Listener implementation

    override fun onNewMissionAvailable(mission: Mission) {
        with(MissionItem(mission, mContext.needItemFactory, createRandomColorArgb())) {
            mMissionItems += this
            notifyItemInserted(mMissionItems.indexOf(this))
        }
    }

    override fun onMissionRemoved(mission: Mission) {
        mMissionItems.find { it.mission == mission }?.let {
            with(mMissionItems.indexOf(it)) {
                mMissionItems.removeAt(this)
                notifyItemRemoved(this)
            }
        }
    }

    // Private functions

    private fun activateNextItem() {
        activeItem?.let {
            val newIndex = mMissionItems.indexOf(it) + 1
            if (newIndex < mMissionItems.size) {
                activateItem(mMissionItems[newIndex])
            }
        }
    }

    private fun activatePreviousItem() {
        activeItem?.let {
            val newIndex = mMissionItems.indexOf(it) - 1
            if (newIndex >= 0) {
                activateItem(mMissionItems[newIndex])
            }
        }
    }

    private fun activateItem(item: MissionItem) {
        activeItem?.apply {
            isSelected = false
            val holder = mRecyclerView.findViewHolderForLayoutPosition(mMissionItems.indexOf(this))
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            mListener?.onStatusItemChanged(activeItem)
        }

        activeItem = item
        activeItem?.apply {
            isSelected = true
            val holder = mRecyclerView.findViewHolderForLayoutPosition(mMissionItems.indexOf(this))
            holder.itemView.setBackgroundColor(Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color)))
            mListener?.onStatusItemChanged(item)
        }
    }

}
