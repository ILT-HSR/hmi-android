package ch.hsr.ifs.gcs.driver.input

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.driver.Input.Listener
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.lang.Exception
import java.util.concurrent.Executors

class HandheldControls(context: Context, private val fPort: UsbSerialPort) : SerialInputOutputManager.Listener, Input {

    companion object {
        private const val LOG_TAG = "HandheldControls"

        fun runOnUIThread(block: () -> Unit) {
            val handler = Handler(Looper.getMainLooper())
            handler.post(block)
        }
    }

    private val fIOExecutor = Executors.newSingleThreadExecutor()
    private lateinit var fIOManager: SerialInputOutputManager

    private val fBuffer = ByteArray(4)
    private var fBuffered = 0
    private val fListeners = ArrayList<Listener>()

    init {
        with(context.getSystemService(Context.USB_SERVICE) as UsbManager) {
            openDevice(fPort.driver.device)?.let {
                fPort.open(it)
                fPort.dtr = true
                fPort.setParameters(9600, 8, 1, UsbSerialPort.PARITY_NONE)
                fIOManager = SerialInputOutputManager(fPort, this@HandheldControls)
                fIOExecutor.submit(fIOManager)
            }
        }
    }

    override fun addListener(listener: Listener) {
        runOnUIThread {
            fListeners.add(listener)
        }
    }

    override fun removeListener(listener: Listener) {
        runOnUIThread {
            fListeners.remove(listener)
        }
    }

    override fun onNewData(data: ByteArray) {
        var message = ""
        data.forEach {
            message += String.format("0x%02x ", it)
        }
//        Log.i(LOG_TAG, "read ${data.size} bytes: $message")

        for (i in 0 until data.size) {
            val byte = data[i]
            if (fBuffered == 0 && byte != (0xfe).toByte()) {
                continue
            }

            fBuffer[fBuffered++] = data[i]

            if (fBuffered == 4) {
                decode()?.let { control ->
                    Log.i(LOG_TAG, "Decoded: $control")
                    runOnUIThread {
                        fListeners.forEach {
                            if (!(control == Control.JOYSTICK_X_AXIS ||
                                            control == Control.JOYSTICK_Y_AXIS)) {
                                it.onButton(control)
                            } else {
                                it.onJoystick(control, fBuffer[2])
                            }
                        }
                    }
                }
                fBuffered = 0
            }
        }
    }

    private fun decode(): Control? = when (fBuffer[1]) {
        Control.DPAD_DOWN.value -> Control.DPAD_DOWN
        Control.DPAD_LEFT.value -> Control.DPAD_LEFT
        Control.DPAD_RIGHT.value -> Control.DPAD_RIGHT
        Control.DPAD_UP.value -> Control.DPAD_UP
        Control.NEED_START.value -> Control.NEED_START
        Control.UPDATE_ABORT.value -> Control.UPDATE_ABORT
        Control.SHOW_ALL.value -> Control.SHOW_ALL
        Control.SHOW_MENU.value -> Control.SHOW_MENU
        Control.ZOOM_IN.value -> Control.ZOOM_IN
        Control.ZOOM_OUT.value -> Control.ZOOM_OUT
//        Control.JOYSTICK_X_AXIS.value -> Control.JOYSTICK_X_AXIS
//        Control.JOYSTICK_Y_AXIS.value -> Control.JOYSTICK_Y_AXIS
        else -> null
    }

    override fun onRunError(e: Exception?) {
        Log.d(LOG_TAG, "read error", e)
    }

}