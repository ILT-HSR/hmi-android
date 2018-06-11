package ch.hsr.ifs.gcs.ui.fragments.missionstatuses

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.input.HandheldControls
import ch.hsr.ifs.gcs.mission.Mission


import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment.OnStatusesFragmentChangedListener
import ch.hsr.ifs.gcs.ui.dummydata.MissionStatusesDummyContent.MissionStatusDummyItem
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.fragment_missionstatuses.view.*

/**
 * [RecyclerView.Adapter] that can display a [MissionStatusDummyItem] and makes a call to the
 * specified [OnStatusesFragmentChangedListener].
 */
class MissionStatusesRecyclerViewAdapter(
        private val mValues: List<Mission>,
        private val mListener: OnStatusesFragmentChangedListener?,
        private val mRecyclerView: RecyclerView,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<MissionStatusesRecyclerViewAdapter.ViewHolder>(), HandheldControls.Listener {

    private val TAG = MissionStatusesRecyclerViewAdapter::class.java.simpleName

    private val mOnClickListener: View.OnClickListener

    lateinit var activeItem: Mission

    init {
//        activeItem = mValues[0]
        //activeItem.isSelected = true
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Mission
            activateItem(item)
            mListener?.onStatusItemChanged(item)
        }
        mContext.controls?.addListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionstatuses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
//        holder.mView.setBackgroundColor(
//                if (item.isSelected) {
//                    Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
//                } else {
//                    Color.TRANSPARENT
//                })
        holder.mMissionName.text = item.status
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun onButton(button: HandheldControls.Button) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when(button) {
            HandheldControls.Button.DPAD_UP -> {
                activatePreviousItem()
            }
            HandheldControls.Button.DPAD_DOWN -> {
                activateNextItem()
            }
            HandheldControls.Button.DPAD_RIGHT -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.refresh_mission)
                mContext.controls?.removeListener(this)
            }
            HandheldControls.Button.NEED_START -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.cancel_action)
                mContext.controls?.removeListener(this)
            }
            HandheldControls.Button.UPDATE_ABORT -> {
                Log.d(TAG, "Cancel Mission Pressed")
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    private fun activateNextItem() {
        val newIndex = mValues.indexOf(activeItem) + 1
        if(newIndex < mValues.size) {
            activateItem(mValues[newIndex])
        }
    }

    private fun activatePreviousItem() {
        val newIndex = mValues.indexOf(activeItem) - 1
        if(newIndex >= 0) {
            activateItem(mValues[newIndex])
        }
    }

    private fun activateItem(item: Mission) {
        //activeItem.isSelected = false
//        mRecyclerView.findViewHolderForLayoutPosition(mValues.indexOf(activeItem)).itemView.setBackgroundColor(Color.TRANSPARENT)
        if(this::activeItem.isInitialized) {
            mListener?.onStatusItemChanged(activeItem)
        }
        activeItem = item
        //activeItem.isSelected = true
 //       mRecyclerView.findViewHolderForLayoutPosition(mValues.indexOf(activeItem)).itemView.setBackgroundColor(Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color)))
        mListener?.onStatusItemChanged(item)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mMissionName: TextView = mView.mission_name
        override fun toString(): String {
            return super.toString() + " '" + mMissionName.text + "'"
        }
    }
}
