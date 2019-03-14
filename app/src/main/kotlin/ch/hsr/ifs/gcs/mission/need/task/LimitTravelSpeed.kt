package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.AerialVehicle.Altitude
import ch.hsr.ifs.gcs.resource.Resource

class LimitTravelSpeed(private val speed: Double) : Task {

    override fun executeOn(resource: Resource) =
            with(resource.plaform as AerialVehicle) {
                listOf(limitTravelSpeed(speed))
            }

}