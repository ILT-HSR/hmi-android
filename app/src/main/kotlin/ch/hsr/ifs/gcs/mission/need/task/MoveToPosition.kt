package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.Vehicle
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ilt.uxv.hmi.core.support.geo.GPSPosition

class MoveToPosition(private val targetLocation: GPSPosition) : Task {

    override fun executeOn(resource: Resource) =
            with(resource.plaform as Vehicle) {
                listOf(moveTo(targetLocation))
            }

}