package ch.hsr.ifs.gcs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Result
import ch.hsr.ifs.gcs.ui.MenuFragmentID
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor

/**
 * The tag class to identify [MainModel] events
 *
 * This class is used to identify events as being of interest to the
 * [main application model][MainModel]. It it sealed, since defining new events outside of the main
 * application model does not make sense. All main application model events must be derived from
 * this class.
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
sealed class MainModelEvent

/**
 * Event type to signal the arrival of a new mission on the system
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class MissionAvailable(val mission: Mission) : MainModelEvent()

/**
 * Event type to signal that the user wants to go to the mission overview
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
object MissionOverviewRequested : MainModelEvent()

/**
 * Event type to signal that a new need has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedAvailable(val need: Need) : MainModelEvent()

/**
 * Event type to signal that a need has become unavailable
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedUnavailable(val need: Need) : MainModelEvent()

/**
 * Event to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedConfigurationStarted(val need: Need) : MainModelEvent()

/**
 * Event to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
object NeedConfigurationAborted : MainModelEvent()

/**
 * Event to signal that a need configuration has finished
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
object NeedConfigurationFinished : MainModelEvent()

/**
 * Event to signal that the user wants to select a need
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
object NeedOverviewRequested : MainModelEvent()

/**
 * Event type to signal that the result of a mission has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class ResultAvailable(val result: Result) : MainModelEvent()

/**
 * The main application model
 *
 * This class provides a data connection from the underlying application logic to the UI.
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class MainModel : Mission.Listener {

    override fun onMissionStatusChanged(mission: Mission, status: Mission.Status) {
        if(status == Mission.Status.FINISHED) {
            fActor.offer(ResultAvailable(Result(mission)))
        }
    }

    private val fAvailableNeeds = MutableLiveData<List<Need>>().apply { value = emptyList() }
    private val fActiveNeed = MutableLiveData<Need>()
    private val fMissions = MutableLiveData<List<Mission>>().apply { value = emptyList() }
    private val fMissionResults = MutableLiveData<List<Result>>().apply { value = emptyList() }
    private val fActiveMenuFragment = MutableLiveData<MenuFragmentID>().apply { value = MenuFragmentID.MISSIONS_FRAGMENT }

    private val fActor = GlobalScope.actor<MainModelEvent>(Main, Channel.UNLIMITED) {
        for (event in this) {
            when (event) {
                is MissionAvailable -> {
                    val mission = event.mission
                    mission.addListener(this@MainModel)
                    fMissions.value = fMissions.value!! + mission
                }
                is NeedAvailable -> {
                    fAvailableNeeds.value = fAvailableNeeds.value!! + event.need
                }
                is NeedUnavailable -> {
                    fAvailableNeeds.value = fAvailableNeeds.value!! - event.need
                }
                is ResultAvailable -> {
                    fMissionResults.value = fMissionResults.value!! + event.result
                }
                is NeedConfigurationStarted -> {
                    fActiveNeed.value = event.need
                    fActiveMenuFragment.value = MenuFragmentID.NEED_INSTRUCTION_FRAGMENT
                }
                is NeedConfigurationAborted -> {
                    fActiveNeed.value = null
                    fActiveMenuFragment.value = MenuFragmentID.NEEDS_FRAGMENT
                }
                is NeedConfigurationFinished -> {
                    fActiveNeed.value?.let {need ->
                        channel.offer(MissionAvailable(Mission(need.copy())))
                        fActiveNeed.value = null
                        fActiveMenuFragment.value = MenuFragmentID.MISSIONS_FRAGMENT
                    }
                }
                is NeedOverviewRequested -> {
                    fActiveMenuFragment.value = MenuFragmentID.NEEDS_FRAGMENT
                }
                is MissionOverviewRequested -> {
                    fActiveMenuFragment.value = MenuFragmentID.MISSIONS_FRAGMENT
                }
            }
        }
    }

    /**
     * The list of available needs
     *
     * @since 1.0.0
     */
    val availableNeeds: LiveData<List<Need>> = fAvailableNeeds

    /**
     * The currently active need
     *
     * @since 1.0.0
     */
    val activeNeed: LiveData<Need> = fActiveNeed

    /**
     * The list of currently active needs
     *
     * @since 1.0.0
     */
    val missions: LiveData<List<Mission>> = fMissions

    /**
     * The currently selected menu fragment
     *
     * @since 1.0.0
     */
    val activeMenuFragment: LiveData<MenuFragmentID> = fActiveMenuFragment

    val missionResults: LiveData<List<Result>> = fMissionResults

    /**
     * Submit an event to the model
     *
     * @since 1.0.0
     */
    fun submit(event: MainModelEvent) = fActor.offer(event)

    fun onDestroy() {
        fActor.close()
    }

}