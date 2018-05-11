package ch.hsr.ifs.gcs.ui.fragments.needs

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent
import kotlinx.android.synthetic.main.fragment_need_instruction.view.*

class NeedInstructionRecyclerViewAdapter(
        private val mValues: List<NeedsDummyContent.Task>
    ) : RecyclerView.Adapter<NeedInstructionRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_need_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mCheckBoxView.background =  holder.mView.context.applicationContext.getDrawable(R.drawable.empty_checkbox)
        holder.mInstructionView.text = item.description
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