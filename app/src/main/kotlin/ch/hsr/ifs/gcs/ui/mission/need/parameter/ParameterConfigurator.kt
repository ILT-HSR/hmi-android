package ch.hsr.ifs.gcs.ui.mission.need.parameter

import android.support.v4.app.Fragment
import android.view.View
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.MainActivity

abstract class ParameterConfigurator<ResultType> : Fragment() {

    lateinit var context: MainActivity

    lateinit var parameter: ParameterItem<ResultType>

    open fun present() = Unit

    open fun destroy() = Unit

    open fun abort() = destroy()

    fun showInstructionText(instructionText: String) {
        val parameterInstructionText = context.findViewById<TextView>(R.id.parameterInstructionText)
        parameterInstructionText.text = instructionText
        parameterInstructionText.visibility = View.VISIBLE
    }

    fun hideInstructionText() {
        val mapInstructionText = context.findViewById<TextView>(R.id.parameterInstructionText)
        mapInstructionText.text = ""
        mapInstructionText.visibility = View.INVISIBLE
    }

}