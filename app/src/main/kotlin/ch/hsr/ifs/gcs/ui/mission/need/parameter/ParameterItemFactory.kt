package ch.hsr.ifs.gcs.ui.mission.need.parameter

import ch.hsr.ifs.gcs.mission.need.parameter.*
import ch.hsr.ifs.gcs.mission.need.parameter.Target
import ch.hsr.ifs.gcs.ui.mission.need.parameter.item.*

object ParameterItemFactory {

    private val PARAMETER_ITEM_CONSTRUCTORS = mutableMapOf<String, (Parameter<*>) -> ParameterItem<*>>(
            "ch.hsr.ifs.gcs.mission.need.parameter.altitude" to { p -> AltitudeItem(p as Altitude) },
            "ch.hsr.ifs.gcs.mission.need.parameter.cargo" to { p -> CargoItem(p as Cargo) },
            "ch.hsr.ifs.gcs.mission.need.parameter.region" to { p -> RegionItem(p as Region) },
            "ch.hsr.ifs.gcs.mission.need.parameter.target" to { p -> TargetItem(p as Target) }
    )

    /**
     * Register a constructor for a specific [Parameter] ID
     *
     * @param id The of associated [Parameter] type
     * @param constructor The constructor to create a need need item of the given [Parameter] type ID
     */
    fun register(id: String, constructor: (Parameter<*>) -> ParameterItem<*>) {
        if (PARAMETER_ITEM_CONSTRUCTORS.contains(id)) {
            throw IllegalArgumentException("Constructor for parameter $id is already registered")
        }

        PARAMETER_ITEM_CONSTRUCTORS[id] = constructor
    }

    /**
     * Instantiate an item for the [Parameter] with the given id
     */
    fun instantiate(parameter: Parameter<*>) = if (PARAMETER_ITEM_CONSTRUCTORS.contains(parameter.id)) {
        PARAMETER_ITEM_CONSTRUCTORS[parameter.id]!!.invoke(parameter)
    } else {
        throw IllegalArgumentException("Unknown parameter type ${parameter.id}")
    }

}