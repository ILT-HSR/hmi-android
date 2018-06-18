package ch.hsr.ifs.gcs.resource

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