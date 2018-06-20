package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.access.ResourceManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Scheduler {

    interface OnSchedulerDataChangedListener {

        fun onNewMissionAvailable(mission: Mission)

        fun onMissionRemoved(mission: Mission)

        fun onMissionStatusChanged(mission: Mission)

    }

    private val LOG_TAG = Scheduler::class.simpleName
    private val fMissions = mutableListOf<Mission>()
    private val fListeners = mutableListOf<OnSchedulerDataChangedListener>()
    private val fExecutionRunner = Executors.newSingleThreadScheduledExecutor()
    private val fExecutions = mutableMapOf<Execution, ScheduledFuture<*>>()

    private operator fun plusAssign(mission: Mission) {
        fMissions += mission
        fListeners.forEach { it.onNewMissionAvailable(mission) }
    }

    private operator fun minusAssign(mission: Mission) {
        fMissions -= mission
        fListeners.forEach { it.onMissionRemoved(mission) }
    }

    fun addListener(listener: OnSchedulerDataChangedListener) {
        fListeners += listener
    }

    fun removeListener(listener: OnSchedulerDataChangedListener) {
        fListeners -= listener
    }

    fun submit(need: Need) {
        if (!ResourceManager.acquire(need.resource)) {
            Log.e(LOG_TAG, "Failed to acquire resource ${need.resource.id}")
            return
        }

        val execution = need.resource.plaform.execution
        need.tasks?.map { it.executeOn(need.resource) }
                ?.forEach(execution::add)

        with(Mission(need)) {
            fExecutions[execution] = fExecutionRunner.scheduleAtFixedRate(object : Runnable {

                private var fLastStatus = Execution.Status.PREPARING
                private val fMission = this@with

                init {
                    this@Scheduler += this@with
                }

                override fun run(): Unit = with(execution.tick()) {
                    if (this != fLastStatus) {
                        fLastStatus = this
                        fListeners.forEach { it.onMissionStatusChanged(fMission) }
                    }
                    when (this) {
                        Execution.Status.FAILURE, Execution.Status.FINISHED -> {
                            fExecutions[execution]?.cancel(false)
                            fExecutions.remove(execution)
                            this@Scheduler -= fMission
                        }
                        Execution.Status.RUNNING -> {
                            fMission.need.resource.markAs(Resource.Status.BUSY)
                        }
                        else -> Unit
                    }
                }

            }, 0, 100, TimeUnit.MILLISECONDS)
        }
    }

}