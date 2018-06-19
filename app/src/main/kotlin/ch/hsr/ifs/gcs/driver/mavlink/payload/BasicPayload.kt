package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform

abstract class BasicPayload(private val fPlatform: MAVLinkPlatform) : MAVLinkPayload {

}