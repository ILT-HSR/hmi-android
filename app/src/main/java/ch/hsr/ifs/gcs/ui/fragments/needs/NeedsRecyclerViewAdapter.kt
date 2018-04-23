package ch.hsr.ifs.gcs.ui.fragments.needs

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R


import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment.OnNeedsFragmentChangedListener
import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent.NeedDummyItem

import kotlinx.android.synthetic.main.fragment_need.view.*

/**
 * [RecyclerView.Adapter] that can display a [NeedDummyItem] and makes a call to the
 * specified [OnNeedsFragmentChangedListener].
 * TODO: Replace the implementation with code for your data type.
 */
class NeedsRecyclerViewAdapter(
        private val mValues: List<NeedDummyItem>,
        private val mListener: OnNeedsFragmentChangedListener?)
    : RecyclerView.Adapter<NeedsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as NeedDummyItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_need, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.id.toString()
        holder.mContentView.text = item.name

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
