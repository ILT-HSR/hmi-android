package ch.hsr.ifs.gcs.need.parameter

import ch.hsr.ifs.gcs.MainActivity

/**
 * Interface representing the base structure of a single need parameter of a need to be configured.
 */
interface Parameter<Result> {

    /**
     * The name of the need parameter. This is shown in the user interface.
     *
     * @since 1.0.0
     */
    val name: String

    /**
     * A short description of what the user needs to do to complete this need parameter.
     *
     * @since 1.0.0
     */
    val description: String

    /**
     * The result of the need parameter when completed.
     *
     * @since 1.0.0
     */
    var result: Result?

    /**
     * Provides a [String] representation of the result. This is shown in the user interface.
     *
     * @since 1.0.0
     */
    fun resultToString(): String

    /**
     * A needParameter is set to active, if previous needParameter are completed, and the
     * current needParameter is not.
     *
     * @since 1.0.0
     */
    var isActive: Boolean

    /**
     * A need parameter is set to completed, if a result has been computed.
     *
     * @since 1.0.0
     */
    var isCompleted: Boolean

    /**
     * The setup function prepares the ui for upcoming user interaction
     * @param context The main activity
     *
     * @since 1.0.0
     */
    fun setup(context: MainActivity)

    /**
     * The cleanup function cleans up the ui after successful user interaction
     * @param context The main activity
     *
     * @since 1.0.0
     */
    fun cleanup(context: MainActivity)

}