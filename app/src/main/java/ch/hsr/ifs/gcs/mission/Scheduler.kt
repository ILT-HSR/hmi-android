package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.needs.Need
import ch.hsr.ifs.gcs.resources.ResourceManager

object Scheduler {

    interface OnSchedulerDataChangedListener {

        fun onNewMissionAvailable(mission: Mission)

        fun onMissionRemoved(mission: Mission)

    }

    private val LOG_TAG = Scheduler::class.simpleName
    private val fMissions = mutableListOf<Mission>()
    private val fListeners = mutableListOf<OnSchedulerDataChangedListener>()

    fun addListener(listener: OnSchedulerDataChangedListener) {
        fListeners += listener
    }

    fun removeListener(listener: OnSchedulerDataChangedListener) {
        fListeners -= listener
    }

    fun submit(need: Need) {
        if(!ResourceManager.acquire(need.resource)) {
            Log.e(LOG_TAG, "Failed to acquire resource ${need.resource.id}")
            return
        }

        with(Mission(need)) {
            fMissions.add(this)
            fListeners.forEach{
                it.onNewMissionAvailable(this)
            }
        }
    }

}