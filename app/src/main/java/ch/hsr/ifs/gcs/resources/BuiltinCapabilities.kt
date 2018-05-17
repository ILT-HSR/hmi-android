package ch.hsr.ifs.gcs.resources

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
 * This map of builtin capabilities
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val BUILTIN_CAPABILITIES = mapOf(
        CAPABILITY_VERTICAL_TAKEOFF.id to CAPABILITY_VERTICAL_TAKEOFF
)