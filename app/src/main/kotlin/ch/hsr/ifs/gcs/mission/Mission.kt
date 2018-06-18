package ch.hsr.ifs.gcs.mission

import android.util.Log
import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.resource.Resource

/**
 * A [mission][Mission] encapsulates the translation of abstract [need][Need] tasks into driver
 * commands.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Mission(private val fResource: Resource, private val fNeed: Need) {

    private val LOG_TAG = Mission::class.simpleName

    init {
        Log.i(LOG_TAG, "Created new mission with the following tasks: ${fNeed.tasks}")
    }

    val status: String
        get() = fNeed.resource.status.name

}