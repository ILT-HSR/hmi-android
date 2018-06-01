package ch.hsr.ifs.gcs.input

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.Executors

class HandheldControls(context: Context, private val fListener: Listener, private val fPort: UsbSerialPort) : SerialInputOutputManager.Listener {

    private val TAG = HandheldControls::class.simpleName

    private val fIOExecutor = Executors.newSingleThreadExecutor()
    private lateinit var fIOManager: SerialInputOutputManager

    private val fBuffer = ByteArray(4)
    private var fBuffered = 0

    enum class Button(val value: Byte) {
        DPAD_LEFT(0x1),
        DPAD_RIGHT(0x2),
        DPAD_UP(0x3),
        DPAD_DOWN(0x4),
        NEED_START(0xA),
        UPDATE_ABORT(0xB),
        SHOW_ALL(0xD),
        SHOW_MENU(0xE),
        ZOOM_IN(0x10),
        ZOOM_OUT(0x11)
    }

    interface Listener {

        fun onButton(button: Button)

    }

    init {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        manager.openDevice(fPort.driver.device)?.let {
            try {
                fPort.open(it)
                fPort.dtr = true
                fPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_NONE)
                fIOManager = SerialInputOutputManager(fPort, this)
                fIOExecutor.submit(fIOManager)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to open control port", e)
            }
        }
    }

    override fun onNewData(data: ByteArray) {
        var message = ""
        data.forEach {
            message += String.format("0x%02x ", it)
        }
        Log.d(TAG, "read ${data.size} bytes: $message")

        for (i in 0 until data.size) {
            val byte = data[i]
            if (fBuffered == 0 && byte != (0xfe).toByte()) {
                continue
            }

            fBuffer[fBuffered++] = data[i]

            if (fBuffered == 4) {
                decode()?.let {
                    fListener.onButton(it)
                }
                fBuffered = 0
            }
        }
    }

    private fun decode(): Button? = when (fBuffer[1]) {
        Button.DPAD_DOWN.value -> Button.DPAD_DOWN
        Button.DPAD_LEFT.value -> Button.DPAD_LEFT
        Button.DPAD_RIGHT.value -> Button.DPAD_RIGHT
        Button.DPAD_UP.value -> Button.DPAD_UP
        Button.NEED_START.value -> Button.NEED_START
        Button.UPDATE_ABORT.value -> Button.UPDATE_ABORT
        Button.SHOW_ALL.value -> Button.SHOW_ALL
        Button.SHOW_MENU.value -> Button.SHOW_MENU
        Button.ZOOM_IN.value -> Button.ZOOM_IN
        Button.ZOOM_OUT.value -> Button.ZOOM_OUT
        else -> null
    }

    override fun onRunError(e: Exception?) {
        Log.d(TAG, "read error", e)
    }

}