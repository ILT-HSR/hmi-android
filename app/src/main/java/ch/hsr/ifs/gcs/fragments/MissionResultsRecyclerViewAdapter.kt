package ch.hsr.ifs.gcs.fragments

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R


import ch.hsr.ifs.gcs.fragments.MissionResultsFragment.OnListFragmentInteractionListener
import ch.hsr.ifs.gcs.dummy.MissionResultsDummyContent.MissionResultDummyItem

import kotlinx.android.synthetic.main.fragment_missionresults.view.*

/**
 * [RecyclerView.Adapter] that can display a [MissionResultDummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class MissionResultsRecyclerViewAdapter(
        private val mValues: List<MissionResultDummyItem>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MissionResultsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as MissionResultDummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            item.isSelected = !item.isSelected
            v.setBackgroundColor(if (item.isSelected) Color.LTGRAY else Color.WHITE)
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_missionresults, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mView.setBackgroundColor(Color.WHITE)
        holder.mIdView.text = item.id
        holder.mContentView.text = "Result"
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
