package ch.hsr.ifs.gcs.ui.fragments

import android.support.v4.app.Fragment
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment
import ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator.CargoConfigurator

interface FragmentHandler {

    enum class FragmentType(val fragment: Fragment) {
        MISSION_RESULTS_FRAGMENT(MissionResultsFragment()),
        MISSION_STATUSES_FRAGMENT(MissionStatusesFragment()),
        NEEDS_FRAGMENT(NeedsFragment()),
        NEED_INSTRUCTION_FRAGMENT(NeedInstructionFragment()),
        CHOOSE_CARGO_FRAGMENT(CargoConfigurator()),

    }

    fun performFragmentTransaction(holderId: Int, fragmentType: FragmentType)

    fun performFragmentTransaction(holderId: Int, fragment: Fragment)

    fun removeFragment(fragment: Fragment)

}