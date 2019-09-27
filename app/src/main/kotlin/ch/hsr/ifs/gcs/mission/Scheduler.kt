package ch.hsr.ifs.gcs.mission

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import java.time.Duration

class Scheduler(private val fTickInterval: Duration = Duration.ofMillis(100)) {

    companion object {
        val MISSION_TICK_CONTEXT = newSingleThreadContext("MissionTickContext")
    }

    private sealed class Event {
        data class LaunchMission(val mission: Mission) : Event()
        data class MissionAborted(val mission: Mission) : Event()
        data class MissionEnded(val mission: Mission) : Event()
    }

    private val fScheduledMissions = mutableMapOf<Mission, Job>()

    private val fActor = GlobalScope.actor<Event>(Default, Channel.UNLIMITED) {
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
                is Event.MissionEnded -> event.mission.let { mission ->
                    fScheduledMissions[mission]?.join()
                }
            }
        }
    }

    fun launch(mission: Mission) {
        fActor.offer(Event.LaunchMission(mission))
    }

    private fun schedule(mission: Mission): Job = GlobalScope.launch(MISSION_TICK_CONTEXT) {
        mission.tick()

        while (isActive) {
            delay(fTickInterval.toMillis())

            if(mission.hasFinished || mission.hasFailed) {
                fActor.offer(Event.MissionEnded(mission))
                break;
            }

            if (!mission.isAborted) {
                mission.tick()
            } else {
                fActor.send(Event.MissionAborted(mission))
            }
        }
    }
}
