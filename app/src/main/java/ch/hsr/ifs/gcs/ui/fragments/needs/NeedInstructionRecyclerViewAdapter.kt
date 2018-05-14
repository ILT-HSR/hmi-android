package ch.hsr.ifs.gcs.ui.fragments.needs

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.model.Task
import kotlinx.android.synthetic.main.fragment_need_instruction.view.*

class NeedInstructionRecyclerViewAdapter(
        private val mValues: List<Task<Any>>
    ) : RecyclerView.Adapter<NeedInstructionRecyclerViewAdapter.ViewHolder>() {

    private var activeItem: Task<Any> = mValues[0]

    init {
        mValues[0].isActive = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_need_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val context = holder.mView.context.applicationContext
        if(item.isActive) {
            holder.mCheckBoxView.background =  context.getDrawable(R.drawable.checkbox_active_incomplete)
        } else {
            holder.mCheckBoxView.background =  context.getDrawable(R.drawable.checkbox_inactive)
        }
        holder.mInstructionView.text = item.name
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mInstructionView: TextView = mView.instruction
        val mCheckBoxView: View = mView.checkBoxView
        override fun toString(): String {
            return super.toString() + " '" + mInstructionView.text + "'"
        }
    }

}