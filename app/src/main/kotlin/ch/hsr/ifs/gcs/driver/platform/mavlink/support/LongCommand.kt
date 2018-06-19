package ch.hsr.ifs.gcs.driver.platform.mavlink.support

enum class LongCommand(val value: Int) {
    NAV_RETURN_TO_LAUNCH(20),
    NAV_LAND(21),
    NAV_TAKEOFF(22),
    DO_SET_MODE(176),
    DO_REPOSITION(192),
    COMPONENT_ARM_DISARM(400),
    REQUEST_AUTOPILOT_CAPABILITIES(520),
}