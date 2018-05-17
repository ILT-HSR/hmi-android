package ch.hsr.ifs.gcs.resources

/**
 * This capability should be attached to resources supporting vertical takeoff
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val CAPABILITY_VERTICAL_TAKEOFF = CapabilityDescriptor(
        "ch.hsr.if.gcs.capability.verticalTakeoff",
        "boolean"
)

/**
 * This list of builtin capabilities
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
val BUILTIN_CAPABILITIES = listOf(
        CAPABILITY_VERTICAL_TAKEOFF
)