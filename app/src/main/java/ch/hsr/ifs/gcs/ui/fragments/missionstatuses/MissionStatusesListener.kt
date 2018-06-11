package ch.hsr.ifs.gcs.ui.fragments.missionstatuses

import android.app.Activity
import ch.hsr.ifs.gcs.mission.Mission
import org.osmdroid.views.MapView

class MissionStatusesListener(val activity: Activity, val map: MapView) : MissionStatusesFragment.OnStatusesFragmentChangedListener {

    override fun onStatusItemChanged(item: Mission?) {
        activity.runOnUiThread({
            item?.let {
//                if(it.isSelected) {
//                    map.overlayManager.addAll(it.mapOverlays)
//                } else {
//                    map.overlayManager.removeAll(it.mapOverlays)
//                }
                map.invalidate()
            }
        })
    }

    override fun refreshStatusesMapView(items: List<Mission>) {
        activity.runOnUiThread({
            map.overlays.clear()
            items.forEach {
//                if (it.isSelected) map.overlayManager.addAll(it.mapOverlays)
            }
            map.invalidate()
        })
    }

}