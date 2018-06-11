package ch.hsr.ifs.gcs.need

import ch.hsr.ifs.gcs.need.parameter.Parameter
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource
import ch.hsr.ifs.gcs.need.task.Task

/**
 * Interface representing the base structure of a need.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface Need {

    /**
     * The name of the need. This is shown in the user interface.
     *
     * @since 1.0.0
     */
    val name: String

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
     * A need is set to active, if it is selected via touch or button navigation.
     *
     * @since 1.0.0
     */
    var isActive: Boolean

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