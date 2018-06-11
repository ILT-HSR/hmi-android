package ch.hsr.ifs.gcs.need.task

import ch.hsr.ifs.gcs.resources.Resource

class TriggerPayload(val payload: String) : Task {

    override fun executeOn(resource: Resource) {
        TODO("not implemented")
    }

}