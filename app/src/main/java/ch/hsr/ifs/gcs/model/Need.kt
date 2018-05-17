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
    val needParameterList: List<NeedParameter<Any>>

    /**
     * A need is set to active, if it is selected via touch or button navigation.
     */
    var isActive: Boolean

    /**
     * Translate the need into a list of [Task] instances.
     * @return A list of [Task] instances.
     */
    fun translateToFunctionList(): List<Task>

}