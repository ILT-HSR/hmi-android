package ch.hsr.ifs.gcs.resource

/**
 * Capabilities provide a way to filter availableResources based on what they can do
 *
 * @param descriptor The [capability descriptor][CapabilityDescriptor] describing this capability
 * @param value The value of this capability
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class Capability<Type>(val descriptor: CapabilityDescriptor, val value: Type)
