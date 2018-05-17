package ch.hsr.ifs.gcs.resources.internal

import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource

data class SimpleResource(override val id: String, override val capabilities: List<Capability<*>>) : Resource