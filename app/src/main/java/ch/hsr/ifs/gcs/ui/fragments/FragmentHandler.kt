package ch.hsr.ifs.gcs.ui.fragments

import android.app.Activity
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsListener
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedInstructionListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsListener
import org.osmdroid.views.MapView

class FragmentHandler(val activity: Activity, val map: MapView) {

    val missionResultsListener = MissionResultsListener(activity, map)
    val missionStatusesListener = MissionStatusesListener(activity, map)
    val needsListener = NeedsListener(activity, map)
    val needInstructionFragment = NeedInstructionListener(activity, map)

}