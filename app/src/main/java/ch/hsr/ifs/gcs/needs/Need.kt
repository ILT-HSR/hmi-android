package ch.hsr.ifs.gcs.needs

import ch.hsr.ifs.gcs.tasks.Task

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
    val needParameterList: List<NeedParameter<*>>

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
    fun getTasks(): List<Task>?

}