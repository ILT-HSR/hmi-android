package ch.hsr.ifs.gcs.ui.fragments.needs

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Button
import ch.hsr.ifs.gcs.need.Need
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment.OnNeedsFragmentChangedListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need.view.*

/**
 * [RecyclerView.Adapter] that can display a [Need] and makes a call to the
 * specified [OnNeedsFragmentChangedListener].
 */
class NeedsRecyclerViewAdapter(
        private val mValues: List<Need>,
        private val mListener: OnNeedsFragmentChangedListener?,
        private val mRecyclerView: RecyclerView,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<NeedsRecyclerViewAdapter.ViewHolder>(), Input.Listener {

    private val mOnClickListener: View.OnClickListener

    var activeItem: Need

    init {
        activeItem = mValues[0]
        activeItem.isActive = true
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Need
            activateItem(item)
            mContext.controls?.removeListener(this)
            mListener?.onNeedItemChanged(item)
        }
        mContext.controls?.addListener(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_need, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mView.setBackgroundColor(
                if (item.isActive) {
                    mContext.resources.getColor(R.color.activeListItem, null)
                } else {
                    Color.TRANSPARENT
                })
        holder.mNameView.text = item.name
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun onButton(button: Button) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (button) {
            Button.DPAD_UP -> {
                activatePreviousItem()
            }
            Button.DPAD_DOWN -> {
                activateNextItem()
            }
            Button.UPDATE_ABORT -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.abort_mission)
                mContext.controls?.removeListener(this)
            }
            Button.NEED_START -> {
                mListener?.onNeedItemChanged(activeItem)
                mContext.controls?.removeListener(this)
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    private fun activateNextItem() {
        val newIndex = mValues.indexOf(activeItem) + 1
        if (newIndex < mValues.size) {
            activateItem(mValues[newIndex])
        }
    }

    private fun activatePreviousItem() {
        val newIndex = mValues.indexOf(activeItem) - 1
        if (newIndex >= 0) {
            activateItem(mValues[newIndex])
        }
    }

    private fun activateItem(item: Need) {
        activeItem.isActive = false
        mRecyclerView.findViewHolderForLayoutPosition(mValues.indexOf(activeItem)).itemView.setBackgroundColor(Color.TRANSPARENT)
        activeItem = item
        activeItem.isActive = true
        mRecyclerView.findViewHolderForLayoutPosition(mValues.indexOf(activeItem)).itemView.setBackgroundColor(mContext.resources.getColor(R.color.activeListItem, null))
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.need_name
        override fun toString(): String {
            return super.toString() + " '" + mNameView.text + "'"
        }
    }

}
