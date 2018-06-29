package ch.hsr.ifs.gcs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import ch.hsr.ifs.gcs.resource.Resource
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

sealed class ResourceEvent

data class NewResourceAvailable(val resource: Resource): ResourceEvent()

class ResourceModel {

    companion object {
        private const val LOG_TAG = "ResourceModel"
    }

    private val fAvailableResources = MutableLiveData<List<Resource>>().apply { value = emptyList() }

    private val fActor = actor<ResourceEvent>(UI, Channel.UNLIMITED){
        for(event in this) {
            when(event) {
                is NewResourceAvailable -> {
                    Log.i(LOG_TAG, "New resource '${event.resource}' became available [${Thread.currentThread().name}")
                    fAvailableResources.value = fAvailableResources.value!! + event.resource
                }
            }
        }
    }

    val availableResources: LiveData<List<Resource>> = fAvailableResources

    /**
     * Submit an event to the model
     *
     * @since 1.0.0
     */
    fun submit(event: ResourceEvent) = fActor.offer(event)

}