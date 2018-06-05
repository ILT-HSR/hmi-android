package ch.hsr.ifs.gcs.resources

import ch.hsr.ifs.gcs.resources.Resource.Status

/**
 * The resource manager provides an abstract interface to the distributed resource management system
 *
 * It is also a [node][ResourceNode] in the system itself, contributing it local availableResources
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
object ResourceManager : ResourceNode {

    private val fLocalResources = ArrayList<Resource>()

    override val availableResources
        get() = synchronized(fLocalResources) {
            fLocalResources.filter {
                it.status == Resource.Status.AVAILABLE
            }
        }

    override val allResources get() = synchronized(fLocalResources) { fLocalResources }

    override fun add(resource: Resource) {
        synchronized(fLocalResources) {
            fLocalResources += resource
        }
    }

    operator fun plusAssign(resource: Resource) = synchronized(fLocalResources) {
        add(resource)
    }

    override operator fun get(vararg capabilities: Capability<*>) =
            synchronized(fLocalResources) {
                availableResources.asSequence()
                        .filter { it.status == Status.AVAILABLE }
                        .filter { capabilities.all(it::has) }
                        .firstOrNull();
            }

    override fun reset() {
        synchronized(fLocalResources) {
            assert(fLocalResources.none { it.status == Status.ACQUIRED || it.status == Status.BUSY }, {
                "Tried to reset ResourceManager with active resources"
            })
            fLocalResources.clear()
        }
    }

    override fun acquire(resource: Resource): Boolean {
        TODO("Implement")
    }
}
