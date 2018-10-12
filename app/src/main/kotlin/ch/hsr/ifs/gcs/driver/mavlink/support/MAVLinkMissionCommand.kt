package ch.hsr.ifs.gcs.driver.mavlink.support

data class MAVLinkMissionCommand(
        val id: LongCommand,
        val frame: NavigationFrame,
        val param1: Float = 0.0f,
        val param2: Float = 0.0f,
        val param3: Float = 0.0f,
        val param4: Float = 0.0f,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val z: Float = 0.0f)