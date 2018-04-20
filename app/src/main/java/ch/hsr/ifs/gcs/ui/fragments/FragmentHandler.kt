package ch.hsr.ifs.gcs.ui.fragments

import android.app.Activity
import android.support.v4.app.Fragment
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsListener
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedInstructionListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsListener
import org.osmdroid.views.MapView

class FragmentHandler(val activity: Activity, val map: MapView) {

    val missionResultsListener = MissionResultsListener(activity, map)
    val missionStatusesListener = MissionStatusesListener(activity, map)
    val needsListener = NeedsListener(activity, map)
    val needInstructionListener = NeedInstructionListener(activity, map)

    fun performFragmentTransaction(holderId: Int, fragment: Fragment) {
        val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
        transaction.replace(holderId, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}