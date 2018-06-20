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
class Mission(val need: Need) {

    val status: String
        get() = need.resource.status.name

}