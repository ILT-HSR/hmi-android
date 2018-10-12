package ch.hsr.ifs.gcs.ui.mission.need

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.R.drawable.*
import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItem
import kotlinx.android.synthetic.main.fragment_need_instruction.view.*
import kotlin.properties.Delegates

class NeedInstructionRecyclerViewAdapter(private val fContext: MainActivity)
    : RecyclerView.Adapter<NeedInstructionRecyclerViewAdapter.ViewHolder>() {

    private var fActiveItem: ParameterItem<*>? = null
    private var fItems: List<ParameterItem<*>> = emptyList()

    var parameters: List<Parameter<*>> by Delegates.observable(emptyList()) { _, old, new ->
        if(old != new) {
            fItems = new.map(fContext.parameterItemFactory::instantiate)
            fActiveItem?.let { act ->
                fItems.find { it.parameter == act.parameter }?.activate()
            } ?: fItems.firstOrNull()?.let {
                it.activate()
                fActiveItem = it
            }
            fActiveItem?.showConfigurator()
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val fInstructionView: TextView = view.instruction
        private val fCheckBoxView: View = view.checkBoxView
        private val fResultView: TextView = view.result

        var item by Delegates.observable<ParameterItem<*>?>(null) { _, _, new ->
            when(new) {
                null -> Unit
                else -> {
                    if(new.isActive && !new.isComplete) {
                        fCheckBoxView.background = fContext.getDrawable(checkbox_active_incomplete)
                    } else if (new.isComplete) {
                        fCheckBoxView.background = fContext.getDrawable(checkbox_active_complete)
                        fResultView.text = new.parameter.resultToString()
                    } else {
                        fCheckBoxView.background = fContext.getDrawable(checkbox_inactive)
                    }
                    fInstructionView.text = new.name
                }
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + fInstructionView.text + "'"
        }
    }

    fun completeCurrent() {
        fActiveItem?.let{
            it.complete()
            it.deactivate()
            it.hideConfigurator()
            val nextIndex = fItems.indexOf(it) + 1
            if(nextIndex < fItems.size) {
                with(fItems[nextIndex]) {
                    activate()
                    fActiveItem = this
                    showConfigurator()
                }
            } else {
                fActiveItem = null
            }
            notifyDataSetChanged()
        }
    }

    fun abort() {
        fItems.asSequence()
                .filter { it.isComplete || it.isActive }
                .map(ParameterItem<*>::abort).toList()
    }

    val isDone get() = fActiveItem == null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_need_instruction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = fItems[position]
    }

    override fun getItemCount(): Int = fItems.size

}