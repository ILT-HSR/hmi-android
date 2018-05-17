package ch.hsr.ifs.gcs.resources

/**
 * A resource node is a part of the distributed resource management system
 *
 * Each node in the distributed resource management system contributes its local resources to the
 * system as a whole. Each contributed resource is a locally connected resource that can be
 * controlled via this node.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface ResourceNode {

    /**
     * The list of locally connected resources
     *
     * @since 1.0.0
     */
    val resources: List<Resource>

    /**
     * Add a new resource to the node
     *
     * @since 1.0.0
     */
    fun add(resource: Resource)

}