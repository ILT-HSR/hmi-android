package ch.hsr.ifs.gcs.ui.mission.need.parameter

import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.overlay.Overlay

abstract class ParameterConfigurator<ResultType> : Fragment() {

    lateinit var context: MainActivity

    lateinit var parameter: ParameterItem<ResultType>

    private lateinit var fOldOverlays: List<Overlay>

    open fun present() {
        fOldOverlays = context.map.overlays.toList()
    }

    open fun destroy() {
        context.map.setBuiltInZoomControls(true)
        context.hideMainFragment()
        hideInstructionText()
        fOldOverlays.forEach{
            if (!context.map.overlays.contains(it)) {
                context.map.overlays.add(it)
            }
        }
    }

    open fun abort() {
        context.map.setBuiltInZoomControls(true)
        context.hideMainFragment()
        hideInstructionText()
        fOldOverlays.forEach{
            if (!context.map.overlays.contains(it)) {
                context.map.overlays.add(it)
            }
        }
    }

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