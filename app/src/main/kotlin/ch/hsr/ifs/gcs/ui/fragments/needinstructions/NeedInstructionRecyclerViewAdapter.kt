package ch.hsr.ifs.gcs.ui.fragments.needinstructions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_instruction.view.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.*

class NeedInstructionRecyclerViewAdapter(
        private val mValues: List<Parameter<*>>,
        private val mContext: MainActivity)
    : RecyclerView.Adapter<NeedInstructionRecyclerViewAdapter.ViewHolder>(), Input.Listener {

    private val TAG = NeedInstructionRecyclerViewAdapter::class.java.simpleName

    init {
        mContext.controls?.addListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_need_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val context = holder.mView.context.applicationContext
        if (item.isActive && !item.isCompleted) {
            holder.mCheckBoxView.background = context.getDrawable(R.drawable.checkbox_active_incomplete)
        } else if (item.isCompleted) {
            holder.mCheckBoxView.background = context.getDrawable(R.drawable.checkbox_active_complete)
            holder.mResultView.text = item.resultToString()
        } else {
            holder.mCheckBoxView.background = context.getDrawable(R.drawable.checkbox_inactive)
        }
        holder.mInstructionView.text = item.name
    }

    override fun onButton(control: Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Control.UPDATE_ABORT -> {
                mContext.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                mContext.leftButton.background = mContext.getDrawable(R.drawable.cancel_action)
                mContext.controls?.removeListener(this)
            }
            Control.NEED_START -> {
                mContext.needNavigationButton.performClick()
            }
            Control.UPDATE_ABORT -> {
                mContext.leftButton.performClick()
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mInstructionView: TextView = mView.instruction
        val mCheckBoxView: View = mView.checkBoxView
        val mResultView: TextView = mView.result
        override fun toString(): String {
            return super.toString() + " '" + mInstructionView.text + "'"
        }
    }

}