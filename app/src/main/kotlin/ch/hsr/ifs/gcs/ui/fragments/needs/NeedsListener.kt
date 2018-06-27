package ch.hsr.ifs.gcs.ui.fragments.needs

import android.app.Activity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import ch.hsr.ifs.gcs.ui.mission.need.NeedItem
import kotlinx.android.synthetic.main.activity_main.*

class NeedsListener : NeedsFragment.OnNeedsFragmentChangedListener {

    lateinit var activity: Activity
    private val fMap by lazy {
        activity.map
    }

    override fun onNeedItemChanged(item: NeedItem) {
        if(activity is MainActivity) {
            with(activity as FragmentHandler) {
                val needInstructionFragmentType = FragmentType.NEED_INSTRUCTION_FRAGMENT
                (needInstructionFragmentType.fragment as NeedInstructionFragment).need = item
                performFragmentTransaction(R.id.menuholder, needInstructionFragmentType)
                activity.leftButton.background = activity.applicationContext.getDrawable(R.drawable.cancel_action)
            }
        }
    }

    override fun refreshNeedsMapView() {
        activity.runOnUiThread {
            fMap.overlays.clear()
            fMap.invalidate()
        }
    }

}