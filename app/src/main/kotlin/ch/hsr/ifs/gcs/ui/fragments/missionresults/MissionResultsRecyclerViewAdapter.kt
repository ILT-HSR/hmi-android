package ch.hsr.ifs.gcs.ui.fragments.missionresults


import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.ui.mission.Results
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment.OnResultsFragmentChangedListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionresults.view.*

/**
 * [RecyclerView.Adapter] that can display a [Results.Item] and makes a call to the
 * specified [OnResultsFragmentChangedListener].
 */
class MissionResultsRecyclerViewAdapter(
        private val mListener: OnResultsFragmentChangedListener?,
        private val mRecyclerView: RecyclerView,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<MissionResultsRecyclerViewAdapter.ViewHolder>(), Input.Listener {

    companion object {
        private val LOG_TAG = MissionResultsRecyclerViewAdapter::class.java.simpleName
    }

    private val mOnClickListener: View.OnClickListener

    private var activeItem: Results.Item? = null

    init {
        if (Results.isNotEmpty()) {
            activeItem = Results[0]
            activeItem!!.isSelected = true
        }
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Results.Item
            activateItem(item)
            mListener?.onResultItemChanged(item)
        }
        mContext.controls?.addListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionresults, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = Results[position]
        holder.mView.setBackgroundColor(
                if (item.isSelected) {
                    Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
                } else {
                    Color.TRANSPARENT
                })
        holder.mMissionName.text = mContext.getString(R.string.results_result_placeholder)
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun onButton(control: Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Control.DPAD_UP -> {
                activatePreviousItem()
            }
            Control.DPAD_DOWN -> {
                activateNextItem()
            }
            Control.DPAD_LEFT -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.abort_mission)
                mContext.controls?.removeListener(this)
            }
            Control.NEED_START -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                mContext.controls?.removeListener(this)
            }
            Control.UPDATE_ABORT -> {
                Log.d(LOG_TAG, "Refresh Mission Pressed")
            }
        }
    }

    override fun getItemCount(): Int = Results.size

    private fun activateNextItem() {
        activeItem?.let {
            val newIndex = Results.indexOf(it) + 1
            if (newIndex < Results.size) {
                activateItem(Results[newIndex])
            }
        }
    }

    private fun activatePreviousItem() {
        activeItem?.let {
            val newIndex = Results.indexOf(it) - 1
            if (newIndex >= 0) {
                activateItem(Results[newIndex])
            }
        }
    }

    private fun activateItem(item: Results.Item) {
        activeItem?.apply {
            isSelected = false
            mRecyclerView.findViewHolderForLayoutPosition(Results.indexOf(this)).itemView.setBackgroundColor(Color.TRANSPARENT)
            mListener?.onResultItemChanged(activeItem)
        }

        activeItem = item
        activeItem?.apply {
            isSelected = true
            mRecyclerView.findViewHolderForLayoutPosition(Results.indexOf(this)).itemView.setBackgroundColor(Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color)))
            mListener?.onResultItemChanged(item)
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mMissionName: TextView = mView.mission_name
        override fun toString(): String {
            return super.toString() + " '" + mMissionName.text + "'"
        }
    }
}
