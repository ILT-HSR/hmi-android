package ch.hsr.ifs.gcs.driver.access

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.mavlink.payload.Gripper
import ch.hsr.ifs.gcs.driver.mavlink.payload.NullPayload

object PayloadProvider {

    private val fDrivers = mutableMapOf<String, () -> Payload>(
            NullPayload.DRIVER_ID to ::NullPayload,
            Gripper.DRIVER_ID to ::Gripper
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