package ch.hsr.ifs.gcs.mission

import ch.hsr.ifs.gcs.needs.Need
import java.util.concurrent.Executors

/**
 * A [mission][Mission] encapsulates the translation of abstract [need][Need] tasks into driver
 * commands.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Mission(private val fNeed: Need) {

    private val fMissionExecutor = Executors.newSingleThreadScheduledExecutor()

    init {
        print(fNeed.getTasks())
    }

    val status: String
        get() = fNeed.resource.status.name

}