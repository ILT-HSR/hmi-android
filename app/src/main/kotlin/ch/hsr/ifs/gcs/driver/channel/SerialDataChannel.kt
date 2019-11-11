package ch.hsr.ifs.gcs.driver.channel

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

/**
 * A byte channel to communicate with serial devices
 *
 * This channel provides an interface to a serial port, facilitating byte oriented input output
 * to serial devices.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class SerialDataChannel private constructor(private val fPort: UsbSerialPort, private val fConfiguration: Configuration) : Channel {

    companion object {

        /**
         * Create a new channel on the given port with the given [configuration][Configuration]
         *
         * @param context The application context to use with the port
         * @param port The USB-Serial port to attach to the channel
         * @param configuration The USB-Serial configuration for the channel
         */
        fun create(context: Context, port: UsbSerialPort, configuration: Configuration): SerialDataChannel? {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            return manager.openDevice(port.driver.device)?.let {
                try {
                    port.open(it)
                    port.setParameters(configuration.baudRate, configuration.dataBits, configuration.stopBits, configuration.parity.value)
                    SerialDataChannel(port, configuration)
                } catch (e: IOException) {
                    null
                }
            }

        }

    }

    data class Configuration @ExperimentalTime constructor(
            val baudRate: Int = 57600,
            val dataBits: Int = 8,
            val stopBits: Int = 1,
            val parity: Parity = Parity.NONE,
            val ioTimeout: Duration = 0.064.seconds
    )

    /**
     * A type for representing serial parity styles
     *
     * @since 1.0.0
     * @author IFS Institute for Software
     */
    @Suppress("unused")
    enum class Parity(val value: Int) {

        /**
         * No parity bit is added to the frame
         */
        NONE(UsbSerialPort.PARITY_NONE),

        /**
         * The parity bit marks if there is an odd number of set bits in the data
         */
        ODD(UsbSerialPort.PARITY_ODD),

        /**
         * The parity bit marks if there is an even number of set bits in the data
         */
        EVEN(UsbSerialPort.PARITY_EVEN),

        /**
         * The parity bit is always set
         */
        MARK(UsbSerialPort.PARITY_MARK),

        /**
         * The parity bit is always cleared
         */
        SPACE(UsbSerialPort.PARITY_SPACE),
    }

    private var fIsOpen = true

    override val isOpen get() = fIsOpen

    @ExperimentalTime
    override fun read(incoming: ByteArray) =
            fPort.read(incoming, fConfiguration.ioTimeout.inMilliseconds.toInt())

    @ExperimentalTime
    override fun write(outgoing: ByteArray) =
            fPort.write(outgoing, fConfiguration.ioTimeout.inMilliseconds.toInt())

    override fun close() =
            fPort.close().also {
                fIsOpen = false
            }

}