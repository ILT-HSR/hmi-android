package ch.hsr.ifs.gcs.resources

/**
 * Capability descriptors define what type a given capability has
 *
 * @param id A unique identifier for the descriptor
 * @param type The type of the capability described by this descriptor
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class CapabilityDescriptor(val id: String, val type: String)

/**
 * Capabilities provide a way to filter resources based on what they can do
 *
 * @param descriptor The [capability descriptor][CapabilityDescriptor] describing this resource
 * @param value The value of this capability
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class Capability<Type>(val descriptor: CapabilityDescriptor, val value: Type)
