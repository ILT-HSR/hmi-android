package ch.hsr.ifs.gcs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import ch.hsr.ifs.gcs.resource.Resource
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor

sealed class ResourceEvent

data class NewResourceAvailable(val resource: Resource): ResourceEvent()

class ResourceModel {

    companion object {
        private const val LOG_TAG = "ResourceModel"
    }

    private val fAvailableResources = MutableLiveData<List<Resource>>().apply { value = emptyList() }

    private val fActor = GlobalScope.actor<ResourceEvent>(Main, Channel.UNLIMITED){
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
     * Submit an submit to the model
     *
     * @since 1.0.0
     */
    fun submit(event: ResourceEvent) = fActor.offer(event)

}