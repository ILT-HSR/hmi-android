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
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.mission.access.NeedProvider
import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.ui.mission.need.NeedItem
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment.OnNeedsFragmentChangedListener
import ch.hsr.ifs.gcs.ui.mission.need.NeedItemFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need.view.*

/**
 * [RecyclerView.Adapter] that can display a [Need] and makes a call to the
 * specified [OnNeedsFragmentChangedListener].
 */
class NeedsRecyclerViewAdapter(
        private val mListener: OnNeedsFragmentChangedListener?,
        private val mRecyclerView: RecyclerView,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<NeedsRecyclerViewAdapter.ViewHolder>(), Input.Listener {

    private val mOnClickListener: View.OnClickListener
    private val mItems = NeedProvider.needs.map{ mContext.needItemFactory.instantiate(it.id, it) }.toMutableList()
    private var mActiveItem = mItems[0]

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView = mView.need_name
        override fun toString(): String {
            return super.toString() + " '" + mNameView.text + "'"
        }
    }

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as NeedItem
            activateItem(item)
            mContext.controls?.removeListener(this)
            mListener?.onNeedItemChanged(item)
        }
        mContext.controls?.addListener(this)
    }

    val activeItem get() = mActiveItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_need, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
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
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.abort_mission)
                mContext.controls?.removeListener(this)
            }
            Control.NEED_START -> {
                mListener?.onNeedItemChanged(mActiveItem)
                mContext.controls?.removeListener(this)
            }
        }
    }

    override fun getItemCount(): Int = mItems.size

    private fun activateNextItem() {
        val newIndex = mItems.indexOf(mActiveItem) + 1
        if (newIndex < mItems.size) {
            activateItem(mItems[newIndex])
        }
    }

    private fun activatePreviousItem() {
        val newIndex = mItems.indexOf(mActiveItem) - 1
        if (newIndex >= 0) {
            activateItem(mItems[newIndex])
        }
    }

    private fun activateItem(item: NeedItem) {
        mActiveItem.deactivate()
        mRecyclerView.findViewHolderForLayoutPosition(mItems.indexOf(mActiveItem)).itemView.setBackgroundColor(Color.TRANSPARENT)
        mActiveItem = item
        mActiveItem.activate()
        mRecyclerView.findViewHolderForLayoutPosition(mItems.indexOf(mActiveItem)).itemView.setBackgroundColor(mContext.resources.getColor(R.color.activeListItem, null))
    }

}
