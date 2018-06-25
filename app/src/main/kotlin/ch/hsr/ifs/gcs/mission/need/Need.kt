package ch.hsr.ifs.gcs.mission.need

import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.mission.need.task.Task

/**
 * Interface representing the base structure of a need.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface Need {

    /**
     * A unique ID for the concrete need
     *
     * @since 1.0.0
     */
    val id: String

    /**
     * The list of tasks necessary to fulfill the need. This is needed for the recycler view.
     *
     * @since 1.0.0
     */
    val parameterList: List<Parameter<*>>

    /**
     * The resource associated with the need.
     *
     * @since 1.0.0
     */
    val resource: Resource

    /**
     * Translate the need into a list of [Task] instances.
     * @return A list of [Task] instances or null, if the need parameters are not completed.
     *
     * @since 1.0.0
     */
    val tasks: List<Task>?

    /**
     * The capabilities required by the concrete need
     *
     * @since 1.0.0
     */
    val requirements: List<Capability<*>>

}