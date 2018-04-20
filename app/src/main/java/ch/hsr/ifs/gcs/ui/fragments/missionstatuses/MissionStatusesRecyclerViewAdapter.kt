package ch.hsr.ifs.gcs.ui.fragments.missionstatuses

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R


import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment.OnResultsFragmentChangedListener
import ch.hsr.ifs.gcs.ui.dummydata.MissionStatusesDummyContent.MissionStatusDummyItem

import kotlinx.android.synthetic.main.fragment_missionstatuses.view.*

/**
 * [RecyclerView.Adapter] that can display a [MissionStatusDummyItem] and makes a call to the
 * specified [OnResultsFragmentChangedListener].
 */
class MissionStatusesRecyclerViewAdapter(
        private val mValues: List<MissionStatusDummyItem>,
        private val mListener: OnResultsFragmentChangedListener?)
    : RecyclerView.Adapter<MissionStatusesRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as MissionStatusDummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            item.isSelected = !item.isSelected
            val lightColor = Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
            v.setBackgroundColor(if (item.isSelected) lightColor else Color.WHITE)
            mListener?.onStatusItemChanged(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionstatuses, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val lightColor = Color.argb(50, Color.red(item.color), Color.green(item.color), Color.blue(item.color))
        holder.mView.setBackgroundColor(if (item.isSelected) lightColor else Color.WHITE)
        holder.mIdView.text = item.id
        holder.mContentView.text = "Status"
        holder.mColorView.setBackgroundColor(item.color)
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content
        val mColorView: View = mView.colorView
        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
