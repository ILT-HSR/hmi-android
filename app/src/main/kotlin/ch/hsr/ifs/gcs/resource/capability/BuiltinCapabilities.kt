package ch.hsr.ifs.gcs.resource.capability

import ch.hsr.ifs.gcs.resource.CapabilityDescriptor

/**
 * This capability should be attached to resources supporting vertical takeoff
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val CAPABILITY_VERTICAL_TAKEOFF = CapabilityDescriptor(
        "ch.hsr.ifs.gcs.capability.verticalTakeoff",
        "boolean"
)

/**
 * This capability should be attached to resources that can fly
 *
 * Platforms acquired through a resource with this capability can be safely cast into
 * [AerialVehicle][ch.hsr.ifs.gcs.driver.AerialVehicle]
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val CAPABILITY_CAN_FLY = CapabilityDescriptor(
        "ch.hsr.ifs.gcs.capability.canFly",
        "boolean"
)

/**
 * This capability should be attached to resources that can move
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val CAPABILITY_CAN_MOVE = CapabilityDescriptor(
        "ch.hsr.ifs.gcs.capability.canMove",
        "boolean"
)

/**
 * This map of builtin capabilities
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val BUILTIN_CAPABILITIES = mapOf(
        CAPABILITY_VERTICAL_TAKEOFF.id to CAPABILITY_VERTICAL_TAKEOFF,
        CAPABILITY_CAN_FLY.id to CAPABILITY_CAN_FLY,
        CAPABILITY_CAN_MOVE.id to CAPABILITY_CAN_MOVE
)