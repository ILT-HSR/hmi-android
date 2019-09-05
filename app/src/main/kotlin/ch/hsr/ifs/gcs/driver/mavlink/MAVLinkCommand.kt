package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand
import ch.hsr.ilt.uxv.hmi.core.driver.Command

data class MAVLinkCommand(override val nativeCommand: MAVLinkMissionCommand) : Command<MAVLinkMissionCommand>