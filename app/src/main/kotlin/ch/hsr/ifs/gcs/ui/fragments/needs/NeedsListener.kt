package ch.hsr.ifs.gcs.ui.fragments.needs

import android.app.Activity
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.NeedItem
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.view.*
import org.osmdroid.views.MapView

class NeedsListener(val activity: Activity, val map: MapView) : NeedsFragment.OnNeedsFragmentChangedListener {

    override fun onNeedItemChanged(item: NeedItem) {
        if(activity is MainActivity) {
            val needInstructionFragmentType = FragmentType.NEED_INSTRUCTION_FRAGMENT
            (needInstructionFragmentType.fragment as NeedInstructionFragment).activeNeed = item
            needInstructionFragmentType.fragment.activeParameterList = item.parameters
            activity.fragmentHandler?.performFragmentTransaction(R.id.menuholder, needInstructionFragmentType)
            activity.leftButton.background = activity.applicationContext.getDrawable(R.drawable.cancel_action)
        }
    }

    override fun refreshNeedsMapView() {
        activity.runOnUiThread {
            map.overlays.clear()
            map.invalidate()
        }
    }

}