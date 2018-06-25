package ch.hsr.ifs.gcs.ui.mission.need.parameter

import android.content.Context
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.mission.need.parameter.Parameter

interface ParameterItem<Result> {

    val name: String

    /**
     * The wrapped parameter
     *
     * @since 1.0.0
     */
    val parameter: Parameter<Result>

    /**
     * Whether the the item is currently selected/activated
     *
     * @since 1.0.0
     */
    val isActive: Boolean

    /**
     * Mark the item as active
     *
     * @since 1.0.0
     */
    fun activate()

    /**
     * Mark the item as inactive
     *
     * @since 1.0.0
     */
    fun deactivate()

    /**
     * Whether the parameter configuration has been completed
     */
    val isComplete: Boolean

    /**
     * Mark this [ParameterItem] as being complete
     */
    fun markComplete()

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