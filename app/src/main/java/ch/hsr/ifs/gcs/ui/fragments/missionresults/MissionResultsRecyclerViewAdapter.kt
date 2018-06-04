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
import ch.hsr.ifs.gcs.input.HandheldControls


import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment.OnResultsFragmentChangedListener
import ch.hsr.ifs.gcs.ui.dummydata.MissionResultsDummyContent.MissionResultDummyItem
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.fragment_missionresults.view.*

/**
 * [RecyclerView.Adapter] that can display a [MissionResultDummyItem] and makes a call to the
 * specified [OnResultsFragmentChangedListener].
 */
class MissionResultsRecyclerViewAdapter(
        private val mValues: List<MissionResultDummyItem>,
        private val mListener: OnResultsFragmentChangedListener?,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<MissionResultsRecyclerViewAdapter.ViewHolder>(), HandheldControls.Listener {

    private val TAG = MissionResultsRecyclerViewAdapter::class.java.simpleName

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as MissionResultDummyItem
            item.isSelected = !item.isSelected
            val lightColor = Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
            v.setBackgroundColor(if (item.isSelected) lightColor else Color.TRANSPARENT)
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
        val item = mValues[position]
        val lightColor = Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
        holder.mView.setBackgroundColor(if (item.isSelected) lightColor else Color.TRANSPARENT)
        holder.mMissionName.text = "Result"
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder?) {
        super.onViewDetachedFromWindow(holder)
        mContext.controls?.removeListener(this)
    }

    override fun onButton(button: HandheldControls.Button) {
        when(button) {
            HandheldControls.Button.DPAD_LEFT -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.cancel_action)
            }
            HandheldControls.Button.UPDATE_ABORT -> {
                Log.d(TAG, "Refresh Mission Pressed")
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mMissionName: TextView = mView.mission_name
        override fun toString(): String {
            return super.toString() + " '" + mMissionName.text + "'"
        }
    }
}
