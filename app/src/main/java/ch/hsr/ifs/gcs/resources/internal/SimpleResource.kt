package ch.hsr.ifs.gcs.resources.internal

import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource
import ch.hsr.ifs.gcs.resources.Resource.Status

class SimpleResource(override val id: String, override val capabilities: List<Capability<*>>) : Resource {

    private var fStatus = Status.UNAVAILABLE

    override val status: Status
        get() = synchronized(fStatus) {
            fStatus
        }

    override fun has(capability: Capability<*>) = capabilities.contains(capability)

    override fun markAs(status: Status) = synchronized(fStatus) {
        fStatus = status
    }
}
