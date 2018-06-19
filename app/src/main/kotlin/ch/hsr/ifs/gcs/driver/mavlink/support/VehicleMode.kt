package ch.hsr.ifs.gcs.driver.mavlink.support

/**
 * This enumeration describes the modes a MAVLink vehicle can be in.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
enum class VehicleMode(val id: Int) {
    /**
     * System is not ready to fly
     */
    PREFLIGHT(0),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under manual RC control without stabilization
     *
     */
    MANUAL_DISARMED(64),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under manual RC control without stabilization
     *
     */
    MANUAL_ARMED(MANUAL_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - in an undefined/developer mode
     */
    TEST_DISARMED(66),

    /**
     * The system is:
     *   - armed
     *   - in an undefined/developer mode
     */
    TEST_ARMED(TEST_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under manual RC control with stabilization
     */
    STABILIZE_DISARMED(80),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under manual RC control with stabilization
     */
    STABILIZE_ARMED(STABILIZE_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under autonomous control with manual setpoint
     */
    GUIDED_DISARMED(88),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under autonomous control with manual setpoint
     */
    GUIDED_ARMED(GUIDED_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - under autonomous control and navigation
     *
     * The system's trajectory is decided onboard and not pre-programmed by waypoints
     */
    AUTO_DISARMED(92),

    /**
     * The system is:
     *   - armed
     *   - under autonomous control and navigation
     *
     * The system's trajectory is decided onboard and not pre-programmed by waypoints
     */
    AUTO_ARMED(AUTO_DISARMED.id + 128),
}