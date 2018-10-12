package ch.hsr.ifs.gcs.driver.channel

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

/**
 * A byte channel to communicate with serial devices
 *
 * This channel provides an interface to a serial port, facilitating byte oriented input output
 * to serial devices.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class SerialDataChannel private constructor(private val fPort: UsbSerialPort) : ByteChannel {

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
                    SerialDataChannel(port)
                } catch (e: IOException) {
                    null
                }
            }

        }

    }

    data class Configuration(
            val baudRate: Int = 57600,
            val dataBits: Int = 8,
            val stopBits: Int = 1,
            val parity: Parity = Parity.NONE
    )

    /**
     * A type for representing serial parity styles
     *
     * @since 1.0.0
     * @author IFS Institute for Software
     */
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
    private val fIncoming = ByteArray(2048)

    override fun isOpen() = fIsOpen

    override fun write(src: ByteBuffer?): Int {
        return when (src) {
            null -> 0
            else -> {
                when (src.remaining()) {
                    0 -> 0
                    else -> {
                        val data = ByteArray(src.remaining())
                        src.get(data)
                        fPort.write(data, 64)
                    }
                }
            }
        }
    }

    override fun close() {
        fPort.close()
        fIsOpen = false
    }

    override fun read(dst: ByteBuffer?): Int {
        return when (dst) {
            null -> 0
            else -> {
                    val read = fPort.read(fIncoming, 64)
                    dst.put(fIncoming, 0, read)
                    read
            }
        }
    }
}