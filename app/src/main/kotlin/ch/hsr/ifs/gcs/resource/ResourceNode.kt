package ch.hsr.ifs.gcs.resource

/**
 * A resource node is a part of the distributed resource management system
 *
 * Each node in the distributed resource management system contributes its local availableResources to the
 * system as a whole. Each contributed resource is a locally connected resource that can be
 * controlled via this node.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface ResourceNode {

    /**
     * The list of locally connected, available resources
     *
     * @since 1.0.0
     */
    val availableResources: List<Resource>

    /**
     * The list of locally connected resources, regardless of their status
     *
     * @since 1.0.0
     */
    val allResources: List<Resource>

    /**
     * Add a new resource to the node
     *
     * @since 1.0.0
     */
    fun add(resource: Resource)

    /**
     * Get a resource with the desired capabilities
     *
     * @param capabilities The desired capabilities
     * @since 1.0.0
     *
     */
    fun get(vararg capabilities: Capability<*>): Resource?

    /**
     * Reset the resource node, removing all of its resources
     *
     * @throws AssertionError If there are resources that are currently acquired or busy
     *
     * @since 1.0.0
     */
    fun reset()

    /**
     * Acquire the given resource
     *
     * @param resource The resource to acquire
     * @return `true` iff. the resource was successfully acquired, `false` otherwise
     *
     * @since 1.0.0
     */
    fun acquire(resource: Resource): Boolean

}