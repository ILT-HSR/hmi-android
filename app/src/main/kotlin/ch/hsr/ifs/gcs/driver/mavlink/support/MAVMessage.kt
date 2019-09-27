package ch.hsr.ifs.gcs.driver.mavlink.support

import me.drton.jmavlib.mavlink.MAVLinkMessage

val MAVLinkMessage.sender get() = MAVLinkSystem(this.systemID, this.componentID)