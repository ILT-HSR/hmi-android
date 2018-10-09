package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand

data class MAVLinkCommand(override val nativeCommand: MAVLinkMissionCommand) : Command<MAVLinkMissionCommand>