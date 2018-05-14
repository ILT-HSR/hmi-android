package ch.hsr.ifs.gcs.model

/**
 * Interface representing the base structure of a single task of a need to be configured.
 */
interface Task<Result> {

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
     * A task is set to active, if previous task are completed, and the current task is not.
     */
    var isActive: Boolean

    /**
     * Must provide the steps necessary to obtain the result of the task
     */
    fun completeTask()

}