package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.SerialPlatform
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.MessageID
import kotlinx.coroutines.CompletableDeferred
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

/**
 * This interface specifies the generic API of MAVLink vehicle platforms.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface MAVLinkPlatform : AerialVehicle, SerialPlatform {

    /**
     * The MAVLink message schema associated with this platform
     *
     * @since 1.0.0
     */
    val schema: MAVLinkSchema

    /**
     * The MAVLink system identifying the GCS
     *
     * @since 1.0.0
     */
    val senderSystem: MAVLinkSystem

    /**
     * The MAVLink system identifying the Vehicle
     *
     * @since 1.0.0
     */
    val targetSystem: MAVLinkSystem

    /**
     * Arm the vehicle for takeoff
     *
     * @since 1.0.0
     */
    fun arm(): MAVLinkCommand

    /**
     * Disarm the vehicle, preventing takeoff
     *
     * @since 1.0.0
     */
    fun disarm(): MAVLinkCommand

    /**
     * Send a fire-and-forget MAVLink message to the attached target system
     *
     * @since 1.2.0
     */
    fun send(message: MAVLinkMessage)

    /**
     * Send an acknowledged MAVLinkMessage to the attached target system
     *
     * @since 1.2.0
     */
    suspend fun sendWithAck(message: MAVLinkMessage, ack: MessageID, retries: Int = 5, matching: (MAVLinkMessage) -> Boolean = {true}) : Boolean

    /**
     * Send a 'Long Command' MAVLinkMessage to the attached target system
     *
     * @since 1.2.0
     */
    suspend fun sendCommand(message: MAVLinkMessage) : Boolean
}