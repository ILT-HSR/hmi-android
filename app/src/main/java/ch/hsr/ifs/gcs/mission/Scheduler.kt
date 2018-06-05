package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.needs.Need

object Scheduler {

    private val fMissions = mutableListOf<Mission>()

    fun submit(need: Need) {
        fMissions.add(Mission(need.getTasks()))
    }

    val missions: List<Mission>
        get() = synchronized(fMissions) {
            fMissions
        }

}