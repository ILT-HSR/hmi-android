package ch.hsr.ifs.gcs.ui.fragments.missionresults

import android.app.Activity
import ch.hsr.ifs.gcs.ui.mission.Results
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

class MissionResultsListener : MissionResultsFragment.OnResultsFragmentChangedListener {

    lateinit var activity: Activity
    private val fMap: MapView by lazy {
        activity.map
    }

    override fun onResultItemChanged(item: Results.Item?) {
        activity.runOnUiThread {
            item?.let {
                if(it.isSelected) {
                    fMap.overlayManager.addAll(it.mapOverlays)
                } else {
                    fMap.overlayManager.removeAll(it.mapOverlays)
                }
                fMap.invalidate()
            }
        }
    }

    override fun refreshResultsMapView() {
        activity.runOnUiThread {
            fMap.overlays.clear()
            Results.forEach {
                if (it.isSelected) fMap.overlayManager.addAll(it.mapOverlays)
            }
            fMap.invalidate()
        }
    }

}