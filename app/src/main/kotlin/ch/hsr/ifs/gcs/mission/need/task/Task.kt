package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.resource.Resource

/**
 * This interface specifies the basic tasks that vehicles are capable of on an abstract level.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface Task {

    /**
     * Execute this Task with the given [resource].
     * @param resource The resource with which the Task should be executed.
     *
     * @since 1.0.0
     */
    fun executeOn(resource: Resource): List<Command<*>>

}