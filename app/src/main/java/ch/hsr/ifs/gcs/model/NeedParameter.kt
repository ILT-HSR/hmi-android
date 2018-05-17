package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity

/**
 * Interface representing the base structure of a single task of a need to be configured.
 */
interface NeedParameter<Result> {

    /**
     * The name of the task. This is shown in the user interface.
     */
    val name: String

    /**
     * A short description of what the user needs to do to complete this task.
     */
    val description: String

    /**
     * The result of the task when completed.
     */
    var result: Result?

    /**
     * Provides a [String] representation of the result. This is shown in the user interface.
     */
    fun resultToString(): String

    /**
     * A task is set to active, if previous task are completed, and the current task is not.
     */
    var isActive: Boolean

    /**
     * A task is set to completed, if a result has been computed.
     */
    var isCompleted: Boolean

    /**
     * The setup function prepares the ui for upcoming user interaction
     * @param context The main activity
     */
    fun setup(context: MainActivity)

    /**
     * The cleanup function cleans up the ui after successful user interaction
     * @param context The main activity
     */
    fun cleanup(context: MainActivity)

}