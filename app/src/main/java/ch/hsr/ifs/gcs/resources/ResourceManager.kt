package ch.hsr.ifs.gcs.resources

/**
 * The resource manager provides an abstract interface to the distributed resource management system
 *
 * It is also a [node][ResourceNode] in the system itself, contributing it local resources
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
object ResourceManager : ResourceNode {

    private val fLocalResources = ArrayList<Resource>()

    override val resources = emptyList<Resource>()

    override fun add(resource: Resource) {
        fLocalResources += resource
    }
    
}