package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.needs.Need
import ch.hsr.ifs.gcs.resources.ResourceManager

object Scheduler {

    private val fMissions = mutableListOf<Mission>()

    private val LOG_TAG = Scheduler::class.simpleName

    fun submit(need: Need) {
        fMissions.add(Mission(need))
        if(!ResourceManager.acquire(need.resource)) {
            Log.e(LOG_TAG, "Failed to acquire resource ${need.resource.id}")
        }
    }

    val missions: List<Mission>
        get() = synchronized(fMissions) {
            fMissions
        }

}