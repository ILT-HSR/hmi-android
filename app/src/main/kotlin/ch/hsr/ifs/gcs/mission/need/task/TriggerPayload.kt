package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.resource.Resource

class TriggerPayload(val payload: String) : Task {

    override fun executeOn(resource: Resource) =
            resource.plaform.payload.trigger()

}