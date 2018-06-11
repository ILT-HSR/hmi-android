package ch.hsr.ifs.gcs.ui.fragments.missionresults

import android.app.Activity
import ch.hsr.ifs.gcs.ui.data.Results
import org.osmdroid.views.MapView

class MissionResultsListener(val activity: Activity, val map: MapView) : MissionResultsFragment.OnResultsFragmentChangedListener {

    override fun onResultItemChanged(item: Results.Item?) {
        activity.runOnUiThread({
            item?.let {
                if(it.isSelected) {
                    map.overlayManager.addAll(it.mapOverlays)
                } else {
                    map.overlayManager.removeAll(it.mapOverlays)
                }
                map.invalidate()
            }
        })
    }

    override fun refreshResultsMapView() {
        activity.runOnUiThread({
            map.overlays.clear()
            Results.forEach {
                if (it.isSelected) map.overlayManager.addAll(it.mapOverlays)
            }
            map.invalidate()
        })
    }

}