package ch.hsr.ifs.gcs.ui.fragments

import android.app.Activity
import android.support.v4.app.Fragment
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsListener
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesListener
import ch.hsr.ifs.gcs.ui.fragments.needparameters.ChooseCargoFragment
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedInstructionFragment
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedInstructionListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsListener
import org.osmdroid.views.MapView

enum class FragmentType(val fragment: Fragment) {
    MISSION_RESULTS_FRAGMENT(MissionResultsFragment()),
    MISSION_STATUSES_FRAGMENT(MissionStatusesFragment()),
    NEEDS_FRAGMENT(NeedsFragment()),
    NEED_INSTRUCTION_FRAGMENT(NeedInstructionFragment()),
    CHOOSE_CARGO_FRAGMENT(ChooseCargoFragment()),
}

class FragmentHandler(val activity: Activity, val map: MapView) {

    var activeFragment =  FragmentType.MISSION_RESULTS_FRAGMENT.fragment
    var previousFragment = FragmentType.MISSION_RESULTS_FRAGMENT.fragment

    val missionResultsListener = MissionResultsListener(activity, map)
    val missionStatusesListener = MissionStatusesListener(activity, map)
    val needsListener = NeedsListener(activity, map)
    val needInstructionListener = NeedInstructionListener(activity, map)

    fun performFragmentTransaction(holderId: Int, fragmentType: FragmentType) {
        previousFragment = activeFragment
        val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
        transaction.replace(holderId, fragmentType.fragment)
        transaction.commit()
        activeFragment = fragmentType.fragment
    }

    fun performFragmentTransaction(holderId: Int, fragment: Fragment) {
        val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
        transaction.replace(holderId, fragment)
        transaction.commit()
        activeFragment = fragment
    }

    fun removeFragment(fragment: Fragment) {
        val transaction = (activity as MainActivity).supportFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commit()
        activeFragment = previousFragment
    }

}