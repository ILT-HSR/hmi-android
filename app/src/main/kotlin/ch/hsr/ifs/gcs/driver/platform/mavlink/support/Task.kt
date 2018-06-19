package ch.hsr.ifs.gcs.driver.platform.mavlink.support

import ch.hsr.ifs.gcs.driver.payload.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.platform.mavlink.MAVLinkPlatform
import ch.hsr.ifs.gcs.mission.need.task.MoveToPosition
import ch.hsr.ifs.gcs.mission.need.task.Task
import ch.hsr.ifs.gcs.mission.need.task.TriggerPayload


fun Task.asMAVLinkCommandDescriptor(platform: MAVLinkPlatform, payload: MAVLinkPayload): CommandDescriptor = when (this) {
    is MoveToPosition -> CommandDescriptor(
            LongCommand.NAV_WAYPOINT,
            x = targetLocation.latitude.toFloat(),
            y = targetLocation.longitude.toFloat(),
            z = targetLocation.altitude.toFloat()
    )
    is TriggerPayload -> payload.commandDescriptor
    else -> throw IllegalArgumentException("Task '$this' not supported")
}
