package ch.hsr.ifs.gcs.comm

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
         * Create a new channel on the given port with the given parameters
         *
         * @param context The application context to use with the port
         * @param port The USB-Serial port to attach to the channel
         * @param baudRate The baud rate to use on the port
         * @param dataBits The number of data bits used on the port
         * @param stopBits The number of stop bits used on the port
         * @param parity The parity style to use on the port
         */
        fun create(context: Context, port: UsbSerialPort, baudRate: Int, dataBits: Int, stopBits: Int, parity: Parity): SerialDataChannel? {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            return manager.openDevice(port.driver.device)?.let {
                try {
                    port.open(it)
                    port.setParameters(baudRate, dataBits, stopBits, parity.value)
                    SerialDataChannel(port)
                } catch (e: IOException) {
                    null
                }
            }
        }

    }

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
    private val fIncoming = ByteArray(128)

    override fun isOpen() = fIsOpen

    override fun write(src: ByteBuffer?): Int {
        return when(src) {
            null -> 0
            else -> {
                when(src.remaining()) {
                    0 -> 0
                    else -> {
                        val data = ByteArray(src.remaining())
                        src.get(data)
                        fPort.write(data, 100)
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
        return when(dst) {
            null -> 0
            else -> {
                try {
                    val read = fPort.read(fIncoming, 1)
                    dst.put(fIncoming, 0, read)
                    read
                } catch (e: IOException) {
                    0
                }
            }
        }
    }
}