package ch.hsr.ifs.gcs.ui.mission.need

import android.app.Activity
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.fragments.MenuFragmentID
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import kotlinx.android.synthetic.main.activity_main.*

class NeedsListener : NeedsFragment.OnNeedsFragmentChangedListener {

    lateinit var activity: Activity
    private val fMap by lazy {
        activity.map
    }

    override fun onNeedItemChanged(item: NeedItem) {
        (activity as? MainActivity)?.apply {
            with(showMenuFragment(MenuFragmentID.NEED_INSTRUCTION_FRAGMENT)) {
                (this as NeedInstructionFragment).need = item
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