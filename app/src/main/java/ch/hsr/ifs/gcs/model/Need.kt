package ch.hsr.ifs.gcs.model

/**
 * Interface representing the base structure of a need.
 */
interface Need {

    /**
     * The name of the need. This is shown in the user interface.
     */
    val name: String

    /**
     * The list of tasks necessary to fulfill the need.
     */
    val taskList: List<Task<Any>>

    /**
     * A need is set to active, if it is selected via touch or button navigation.
     */
    var isActive: Boolean

}