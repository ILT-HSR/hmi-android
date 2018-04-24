package ch.hsr.ifs.gcs.ui.fragments.needs

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.View
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

class NeedsListener(val activity: Activity, val map: MapView) : NeedsFragment.OnNeedsFragmentChangedListener {

    override fun onNeedItemChanged(item: NeedsDummyContent.NeedDummyItem?) {
        if(activity is MainActivity) {
            val needInstructionFragmentType = FragmentType.NEED_INSTRUCTION_FRAGMENT
            (needInstructionFragmentType.fragment as NeedInstructionFragment).activeNeed = item
            activity.fragmentHandler?.performFragmentTransaction(R.id.menuholder, needInstructionFragmentType)
            activity.leftButton.background = activity.applicationContext.getDrawable(R.drawable.cancel_action)
        }
    }

    override fun refreshNeedsMapView() {
        activity.runOnUiThread({
            map.overlays.clear()
            map.invalidate()
        })
    }

}