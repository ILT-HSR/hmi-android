package ch.hsr.ifs.gcs.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor

sealed class PlatformEvent

data class NewPlatformAvailable(val platform: Platform) : PlatformEvent()

class PlatformModel {

    companion object {
        private const val LOG_TAG = "PlatformModel"
    }

    private val fAvailablePlatforms = MutableLiveData<List<Platform>>().apply { value = emptyList() }

    private val fActor = GlobalScope.actor<PlatformEvent>(Main, Channel.UNLIMITED) {
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