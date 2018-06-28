package ch.hsr.ifs.gcs.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Result
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
sealed class Event

/**
 * Event type to signal the arrival of a new missin on the system
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class MissionAvailable(val mission: Mission) : Event()

/**
 * Event type to signal a new need has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedAvailable(val need: Need) : Event()

/**
 * Event type to signal a need has become unavailable
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedUnavailable(val need: Need) : Event()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class NeedConfigurationStarted(val need: Need) : Event()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class NeedConfigurationAborted: Event()

/**
 * Event to to signal that a need configuration has been started
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class NeedConfigurationFinished() : Event()

/**
 * Event type to signal that the result of a mission has become available
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
data class ResultAvailable(val result: Result) : Event()

/**
 * The main application model
 *
 * This class provides a data connection from the underlying application logic to the UI.
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class MainModel : ViewModel() {

    private val fAvailableNeeds = MutableLiveData<List<Need>>().apply { value = emptyList() }
    private val fActiveNeed = MutableLiveData<Need>()
    private val fActiveMissions = MutableLiveData<List<Mission>>().apply { value = emptyList() }
    private val fMissionResults = MutableLiveData<List<Result>>().apply { value = emptyList() }

    private val fActor = actor<Event>(UI, Channel.UNLIMITED) {
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
                }
                is NeedConfigurationAborted, is NeedConfigurationFinished -> {
                    fActiveNeed.value = null
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
     * Submit an event to the model
     *
     * @since 1.0.0
     */
    fun event(event: Event) = fActor.offer(event)

    // ViewModel implementation

    override fun onCleared(){
        fActor.close()
    }

}