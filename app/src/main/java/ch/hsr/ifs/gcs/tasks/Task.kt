package ch.hsr.ifs.gcs.tasks

import ch.hsr.ifs.gcs.resources.Resource

/**
 * This task interface specifies the basic tasks that vehicles are capable of on an abstract level.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface Task {

    /**
     * Execute this task with the given [resource].
     * @param resource The resource with which the task should be executed.
     *
     * @since 1.0.0
     */
    fun executeOn(resource: Resource)

}