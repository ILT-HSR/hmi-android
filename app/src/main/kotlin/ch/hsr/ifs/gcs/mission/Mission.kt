package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.mission.need.Need

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