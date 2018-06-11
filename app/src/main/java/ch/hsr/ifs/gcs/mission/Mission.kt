package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.needs.Need
import java.util.concurrent.Executors

/**
 * A [mission][Mission] encapsulates the translation of abstract [need][Need] tasks into driver
 * commands.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
open class Mission(private val fNeed: Need) {

    private val LOG_TAG = Mission::class.simpleName

    private val fMissionExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        Log.i(LOG_TAG, "Created new mission with the following tasks: ${fNeed.getTasks()}")
    }

    val status: String
        get() = fNeed.resource.status.name

}