package ch.hsr.ifs.gcs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Result
import ch.hsr.ifs.gcs.ui.MenuFragmentID
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

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
 * Event type to signal the arrival of a new missin on the system
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class MissionAvailable(val mission: Mission) : MainModelEvent()

/**
 * Event type to signal the user wants to go to the mission Overview
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class MissionOverviewRequested : MainModelEvent()

/**
 * Event type to signal a new need has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedAvailable(val need: Need) : MainModelEvent()

/**
 * Event type to signal a need has become unavailable
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedUnavailable(val need: Need) : MainModelEvent()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedConfigurationStarted(val need: Need) : MainModelEvent()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class NeedConfigurationAborted : MainModelEvent()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class NeedConfigurationFinished() : MainModelEvent()

/**
 * Event to signal that the user wants to select a need
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class NeedOverviewRequested : MainModelEvent()

/**
 * Event type to signal that the result of a mission has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class ResultAvailable(val result: Result) : MainModelEvent()

/**
 * Event type to signal that an input device became available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class InputDeviceAvailable(val device: Input) : MainModelEvent()

/**
 * Event type to signal that an input device became available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
object InputDeviceUnavailable : MainModelEvent()

/**
 * The main application model
 *
 * This class provides a data connection from the underlying application logic to the UI.
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class MainModel {

    private val fAvailableNeeds = MutableLiveData<List<Need>>().apply { value = emptyList() }
    private val fActiveNeed = MutableLiveData<Need>()
    private val fActiveMissions = MutableLiveData<List<Mission>>().apply { value = emptyList() }
    private val fMissionResults = MutableLiveData<List<Result>>().apply { value = emptyList() }
    private val fActiveMenuFragment = MutableLiveData<MenuFragmentID>().apply { value = MenuFragmentID.MISSION_STATUSES_FRAGMENT }
    private val fActiveInputDevice = MutableLiveData<Input?>()

    private val fActor = actor<MainModelEvent>(UI, Channel.UNLIMITED) {
        for (event in this) {
            when (event) {
                is MissionAvailable -> {
                    fActiveMissions.value = fActiveMissions.value!! + event.mission
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
                    fActiveNeed.value?.let(::Mission)?.let {
                        fActiveMissions.value = fActiveMissions.value!! + it
                    }
                    fActiveNeed.value = null
                    fActiveMenuFragment.value = MenuFragmentID.MISSION_STATUSES_FRAGMENT
                }
                is NeedOverviewRequested -> {
                    fActiveMenuFragment.value = MenuFragmentID.NEEDS_FRAGMENT
                }
                is MissionOverviewRequested -> {
                    fActiveMenuFragment.value = MenuFragmentID.MISSION_STATUSES_FRAGMENT
                }
                is InputDeviceAvailable -> {
                    fActiveInputDevice.value = event.device
                }
                is InputDeviceUnavailable -> {
                    fActiveInputDevice.value = null
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
    val activeMissions: LiveData<List<Mission>> = fActiveMissions

    /**
     * The list of available mission results
     *
     * @since 1.0.0
     */
    val missionResults: LiveData<List<Result>> = fMissionResults

    /**
     * The currently selected menu fragment
     *
     * @since 1.0.0
     */
    val activeMenuFragment: LiveData<MenuFragmentID> = fActiveMenuFragment

    /**
     * The currently active input device
     *
     * @since 1.0.0
     */
    val activeInputDevice: LiveData<Input?> = fActiveInputDevice

    /**
     * Submit an submit to the model
     *
     * @since 1.0.0
     */
    fun submit(event: MainModelEvent) = fActor.offer(event)

    fun onDestroy() {
        fActor.close()
    }

}