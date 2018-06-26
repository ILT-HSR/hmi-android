package ch.hsr.ifs.gcs.mission.need.parameter

/**
 * Interface representing the base structure of a single need parameter of a need to be configured.
 */
interface Parameter<Result> {

    val id: String

    /**
     * The result of the need parameter when completed.
     *
     * @since 1.0.0
     */
    var result: Result

    /**
     * Provides a [String] representation of the result. This is shown in the user interface.
     *
     * @since 1.0.0
     */
    fun resultToString(): String

}