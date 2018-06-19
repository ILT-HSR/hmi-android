package ch.hsr.ifs.gcs.resource.internal

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.Resource.Status

class SimpleResource(override val id: String,
                     override val driverId: String,
                     override val payloadDriverId: String?,
                     override val capabilities: List<Capability<*>>) : Resource {

    private lateinit var fPlatform: Platform

    private var fStatus = Status.UNAVAILABLE

    override val status: Status
        get() = synchronized(fStatus) {
            fStatus
        }

    override fun has(capability: Capability<*>) = capabilities.contains(capability)

    override fun markAs(status: Status) = synchronized(fStatus) {
        fStatus = status
    }

    override var plaform: Platform
        get() = fPlatform
        set(value) {
            fPlatform = value
        }
}
