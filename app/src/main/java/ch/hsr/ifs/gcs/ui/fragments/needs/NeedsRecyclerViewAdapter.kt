package ch.hsr.ifs.gcs.ui.fragments.needs

import android.graphics.Color
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
 */
class NeedsRecyclerViewAdapter(
        private val mValues: List<NeedDummyItem>,
        private val mListener: OnNeedsFragmentChangedListener?)
    : RecyclerView.Adapter<NeedsRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as NeedDummyItem
            mListener?.onNeedItemChanged(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_need, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mNameView.text = item.name
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.need_name
        override fun toString(): String {
            return super.toString() + " '" + mNameView.text + "'"
        }
    }

}
