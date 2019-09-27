package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.driver.generic.payload.NullPayload
import ch.hsr.ifs.gcs.driver.mavlink.payload.Gripper
import ch.hsr.ifs.gcs.driver.mavlink.payload.RadiationSensor

object PayloadDrivers {

    private val fDrivers = mutableMapOf<String, () -> Payload>(
            NullPayload.DRIVER_ID to ::NullPayload,
            Gripper.DRIVER_ID to ::Gripper,
            RadiationSensor.DRIVER_ID to :: RadiationSensor
    )

    /**
     * Create a new driver instance for the [platform][Platform]
     *
     * @param id The ID associated with the desired payload
     *
     * @return A new instance of the payload driver or null of no driver was found
     */
    fun instantiate(id: String) =
            fDrivers[id]?.invoke()

}