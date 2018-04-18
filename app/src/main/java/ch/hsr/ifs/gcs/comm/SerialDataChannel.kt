package ch.hsr.ifs.gcs.comm

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel

class SerialDataChannel : ByteChannel {

    companion object {

        fun create(context: Context, port: UsbSerialPort, baudrate: Int, dataBits: Int, stopBits: Int, parity: Parity): SerialDataChannel? {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            return manager.openDevice(port.driver.device)?.let {
                try {
                    port.open(it)
                    port.setParameters(baudrate, dataBits, stopBits, parity.value)
                    SerialDataChannel(port)
                } catch (e: IOException) {
                    null
                }
            }
        }

    }

    enum class Parity(val value: Int) {
        NONE(UsbSerialPort.PARITY_NONE),
        ODD(UsbSerialPort.PARITY_ODD),
        EVEN(UsbSerialPort.PARITY_EVEN),
        MARK(UsbSerialPort.PARITY_MARK),
        SPACE(UsbSerialPort.PARITY_SPACE),
    }

    private val fPort: UsbSerialPort
    private var fIsOpen = true
    private val fIncoming = ByteArray(128)

    private constructor(port: UsbSerialPort) {
        fPort = port
    }

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