package ch.hsr.ifs.gcs.mission

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Scheduler {

    interface Listener {

        fun onMissionUpdated(mission: Mission)

    }

    private val TICK = Duration.ofMillis(100)

    private val fListeners = mutableListOf<Listener>()
    private val fMissionExecutionStatuses = mutableMapOf<Mission, Execution.Status>()
    private val fExecutionRunner = Executors.newSingleThreadScheduledExecutor()

    fun addListener(listener: Listener) {
        fListeners += listener
    }

    fun removeListener(listener: Listener) {
        fListeners -= listener
    }

    fun launch(mission: Mission) {
        tick(mission)
    }

    private fun tick(mission: Mission) {
        val status = mission.tick()
        if(!fMissionExecutionStatuses.contains(mission)) {
            val oldStatus = fMissionExecutionStatuses[mission]
            if(status != oldStatus) {
                fListeners.forEach { it.onMissionUpdated(mission) }
                fMissionExecutionStatuses[mission] = status
            }
        } else {
            fListeners.forEach { it.onMissionUpdated(mission) }
            fMissionExecutionStatuses[mission] = status
        }

        if(status != Execution.Status.FINISHED && status != Execution.Status.FAILURE) {
            fExecutionRunner.schedule({ tick(mission) }, TICK.toMillis(), TimeUnit.MILLISECONDS)
        }
    }

}