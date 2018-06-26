package ch.hsr.ifs.gcs.mission.access

import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Scheduler

object MissionProvider : Scheduler.Listener {

    interface Listener {

        fun onNewMissionAvailable(mission: Mission)

        fun onMissionRemoved(mission: Mission)

    }

    private val fMissions = mutableListOf<Mission>()
    private val fListeners = mutableListOf<Listener>()

    init {
        Scheduler.addListener(this)
    }

    fun addListener(listener: Listener) {
        fListeners += listener
    }

    fun removeListener(listener: Listener) {
        fListeners -= listener
    }

    fun submit(mission: Mission) = synchronized(fMissions) {
        fMissions += mission
        fListeners.forEach { it.onNewMissionAvailable(mission) }
    }

    fun remove(mission: Mission) = synchronized(fMissions) {
        if(fMissions.remove(mission)) {
            fListeners.forEach { it.onMissionRemoved(mission) }
        }
    }

    override fun onMissionUpdated(mission: Mission) {

    }
}