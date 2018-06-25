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

    private val fPlatform = need.resource.plaform
    private val fExecution = fPlatform.execution

    init {
        need.tasks?.apply {
            map { it.executeOn(need.resource) }
                    .forEach(fExecution::add)
        }
    }

    val status: String
        get() = need.resource.status.name

    fun tick() = fExecution.tick()

}