package ch.hsr.ifs.gcs.resources.internal

import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource

class SimpleResource(override val id: String, override val capabilities: List<Capability<*>>) : Resource {

    private var fStatus = Resource.Status.UNAVAILABLE

    override val status: Resource.Status
        get() = fStatus

    override fun has(capability: Capability<*>) = capabilities.contains(capability)

}