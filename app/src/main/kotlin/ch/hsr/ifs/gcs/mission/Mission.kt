package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.driver.RecordingPayload
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

/**
 * A [mission][Mission] encapsulates the translation of abstract [need][Need] tasks into driver
 * commands.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Mission(val need: Need) {

    interface Listener {
        fun onMissionStatusChanged(mission: Mission, status: Status)
    }

    enum class Status {
        PREPARING,
        ACTIVE,
        FINISHED,
        ABORTED,
        FAILED
    }

    companion object {
        private val MISSION_CONTEXT = newSingleThreadContext("MissionContext")
        private const val LOG_TAG = "Mission"
    }

    private val fIsAborted = AtomicBoolean()
    private val fPlatform = need.resource.plaform
    private val fExecution = fPlatform.execution
    private var fActiveTick: Job? = null
    private val fListeners = mutableListOf<Listener>()
    private var fStatus by Delegates.observable(Status.PREPARING) { _, old, new ->
        if (old != new) {
            GlobalScope.launch(MISSION_CONTEXT) {
                fListeners.forEach { it.onMissionStatusChanged(this@Mission, new) }
            }
        }
    }

    init {
        fExecution.reset()
        need.tasks?.apply {
            flatMap { it.executeOn(need.resource) }
                    .forEach(fExecution::add)
        }
    }

    val isAborted get() = fIsAborted.get()

    val hasFinished get() = fStatus == Status.FINISHED

    val hasFailed get() = fStatus == Status.FAILED

    val status get() = runBlocking(MISSION_CONTEXT) { fStatus }

    val resultData
        get() = when (val payload = fPlatform.payloads.firstOrNull()) {
            is RecordingPayload -> Result.Data(payload.recording)
            else -> Result.Data(Unit)
        }

    fun abort() {
        if (!fIsAborted.getAndSet(true)) {
            Log.i(LOG_TAG, "Abortion requested for $this")
            fActiveTick?.cancel()
        }
    }

    fun addListener(listener: Listener) = runBlocking(MISSION_CONTEXT) {
        fListeners += listener
    }

    fun removeListener(listener: Listener) = runBlocking(MISSION_CONTEXT) {
        fListeners -= listener
    }

    suspend fun tick() {
        if (!fIsAborted.get()) {
            fActiveTick?.let {
                if (!it.isActive) {
                    performTick()
                } else {
                    it.join()
                }
            } ?: performTick()
        }
    }

    private fun performTick() {
        fActiveTick = GlobalScope.launch(MISSION_CONTEXT) {
            fStatus = when (fExecution.tick()) {
                Execution.Status.FAILURE -> Status.FAILED
                Execution.Status.PREPARING -> Status.PREPARING
                Execution.Status.RUNNING -> Status.ACTIVE
                Execution.Status.FINISHED -> Status.FINISHED
            }
        }
    }
}
