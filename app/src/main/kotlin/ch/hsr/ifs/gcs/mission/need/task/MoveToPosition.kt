package ch.hsr.ifs.gcs.mission.need.task

import android.location.Location
import ch.hsr.ifs.gcs.driver.Vehicle
import ch.hsr.ifs.gcs.resource.Resource

class MoveToPosition(private val targetLocation: Location) : Task {

    override fun executeOn(resource: Resource) =
            with(resource.plaform as Vehicle) {
                moveTo(targetLocation)
            }

}