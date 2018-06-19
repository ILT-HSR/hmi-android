package ch.hsr.ifs.gcs.driver.payload.mavlink

import ch.hsr.ifs.gcs.driver.platform.mavlink.MAVLinkPlatform

abstract class BasicPayload(private val fPlatform: MAVLinkPlatform) : MAVLinkPayload {

}