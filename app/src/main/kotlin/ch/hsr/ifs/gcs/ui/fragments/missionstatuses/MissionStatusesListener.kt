package ch.hsr.ifs.gcs.ui.fragments.missionstatuses

import android.app.Activity
import ch.hsr.ifs.gcs.ui.mission.MissionItem
import org.osmdroid.views.MapView

class MissionStatusesListener(val activity: Activity, val map: MapView) : MissionStatusesFragment.OnStatusesFragmentChangedListener {

    override fun onStatusItemChanged(item: MissionItem?) {
        activity.runOnUiThread {
            item?.apply {
                if (isSelected) {
                    map.overlayManager.addAll(mapOverlays)
                } else {
                    map.overlayManager.removeAll(mapOverlays)
                }
            }
            map.invalidate()
        }
    }

    override fun refreshStatusesMapView(items: List<MissionItem>) {
        activity.runOnUiThread {
            map.overlays.clear()
            items.forEach {
                if (it.isSelected) {
                    map.overlayManager.addAll(it.mapOverlays)
                }
            }
            map.invalidate()
        }
    }

}