package ch.hsr.ifs.gcs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Result
import ch.hsr.ifs.gcs.support.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MainModel : ViewModel() {

    private val fAvailableNeeds = MutableLiveData<List<Need>>().apply { value = emptyList() }
    private val fActiveMissions = MutableLiveData<List<Mission>>().apply { value = emptyList() }
    private val fMissionResults = MutableLiveData<List<Result>>().apply { value = emptyList() }

    private val fActor = actor<Action>(UI, Channel.UNLIMITED) {
        for (action in this) {
            when (action) {
                is MissionAvailable -> {
                    fActiveMissions.value = fActiveMissions.value!! + action.mission
                }
                is NeedAvailable -> {
                    fAvailableNeeds.value = fAvailableNeeds.value!! + action.need
                }
                is NeedUnavailable -> {
                    fAvailableNeeds.value = fAvailableNeeds.value!! - action.need
                }
                is ResultAvailable -> {
                    fMissionResults.value = fMissionResults.value!! + action.result
                }
            }
        }
    }

    val availableNeeds: LiveData<List<Need>> = fAvailableNeeds
    val activeMissions: LiveData<List<Mission>> = fActiveMissions
    val missionResults: LiveData<List<Result>> = fMissionResults

    fun action(action: Action) = fActor.offer(action)

    override fun onCleared(){
        fActor.close()
    }

}