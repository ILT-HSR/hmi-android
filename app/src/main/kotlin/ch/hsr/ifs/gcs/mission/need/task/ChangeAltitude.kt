package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.AerialVehicle.Altitude
import ch.hsr.ifs.gcs.resource.Resource

class ChangeAltitude(private val altitude: Int) : Task {

    override fun executeOn(resource: Resource) =
            with(resource.plaform as AerialVehicle) {
                changeAltitude(Altitude(altitude.toDouble()))
            }

}