package ch.hsr.ifs.gcs.ui.mission

import android.app.Activity
import kotlinx.android.synthetic.main.activity_main.*

class MissionStatusesListener : MissionStatusesFragment.OnStatusesFragmentChangedListener {

    lateinit var activity: Activity
    private val fMap by lazy {
        activity.map
    }

    override fun onStatusItemChanged(item: MissionItem?) {
        activity.runOnUiThread {
            item?.apply {
                if (isSelected) {
                    fMap.overlayManager.addAll(mapOverlays)
                } else {
                    fMap.overlayManager.removeAll(mapOverlays)
                }
            }
            fMap.invalidate()
        }
    }

    override fun refreshStatusesMapView(items: List<MissionItem>) {
        activity.runOnUiThread {
            fMap.overlays.clear()
            items.forEach {
                if (it.isSelected) {
                    fMap.overlayManager.addAll(it.mapOverlays)
                }
            }
            fMap.invalidate()
        }
    }

}