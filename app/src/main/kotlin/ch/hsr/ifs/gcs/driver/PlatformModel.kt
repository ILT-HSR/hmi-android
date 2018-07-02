package ch.hsr.ifs.gcs.driver

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

sealed class PlatformEvent

data class NewPlatformAvailable(val platform: Platform) : PlatformEvent()

class PlatformModel {

    companion object {
        private const val LOG_TAG = "PlatformModel"
    }

    private val fAvailablePlatforms = MutableLiveData<List<Platform>>().apply { value = emptyList() }

    private val fActor = actor<PlatformEvent>(UI, Channel.UNLIMITED) {
        for(event in this) {
            when(event) {
                is NewPlatformAvailable -> {
                    Log.i(LOG_TAG, "New platform '${event.platform} became available")
                    fAvailablePlatforms.value = fAvailablePlatforms.value!! + event.platform
                }
            }
        }
    }

    val availablePlatforms: LiveData<List<Platform>> = fAvailablePlatforms

    fun submit(event: PlatformEvent) = fActor.offer(event)

}