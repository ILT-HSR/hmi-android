package ch.hsr.ifs.gcs.mission

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import java.time.Duration
import java.util.concurrent.TimeUnit

class Scheduler(private val fTickInterval: Duration = Duration.ofMillis(100)) {

    companion object {
        val MISSION_TICK_CONTEXT = newSingleThreadContext("MissionTickContext")
    }

    private sealed class Event {
        data class LaunchMission(val mission: Mission) : Event()
        data class MissionAborted(val mission: Mission) : Event()
    }

    private val fScheduledMissions = mutableMapOf<Mission, Job>()

    private val fActor = actor<Event>(DefaultDispatcher, Channel.UNLIMITED) {
        for (event in this) {
            when (event) {
                is Event.LaunchMission -> event.mission.let { mission ->
                    if (mission !in fScheduledMissions) {
                        fScheduledMissions[mission] = schedule(mission)
                    }
                }
                is Event.MissionAborted -> event.mission.let { mission ->
                    fScheduledMissions[mission]?.let {
                        if(it.isActive) {
                            it.cancel()
                        }
                    }
                }
            }
        }
    }

    fun launch(mission: Mission) {
        fActor.offer(Event.LaunchMission(mission))
    }

    fun shutdown() {
        fActor.close()
    }

    private fun schedule(mission: Mission): Job = launch(MISSION_TICK_CONTEXT) {
        mission.tick()

        while (isActive) {
            delay(fTickInterval.toNanos(), TimeUnit.NANOSECONDS)
            if (!mission.isAborted) {
                mission.tick()
            } else {
                fActor.send(Event.MissionAborted(mission))
            }
        }

        mission.abort()
        fActor.send(Event.MissionAborted(mission))
    }
}
