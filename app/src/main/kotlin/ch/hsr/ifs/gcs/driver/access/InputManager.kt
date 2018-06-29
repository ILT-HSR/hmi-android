package ch.hsr.ifs.gcs.driver.access

import android.content.Context
import android.hardware.usb.UsbDevice
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.input.HandheldControls
import com.hoho.android.usbserial.driver.UsbSerialProber

class InputManager(private val fListener: Listener) {

    interface Listener {

        fun onInputDeviceAvailable(device: Input)

        fun onInputDeviceUnavailable()

    }

    private val fListeners = mutableListOf<Listener>()
    private var fCurrentDevice: Pair<UsbDevice, Input>? = null

    fun deviceAttached(context: Context, device: UsbDevice) {
        if(fCurrentDevice != null) {
            return
        }

        open(context, device)?.let {
            fCurrentDevice = Pair(device, it)
            fListener.onInputDeviceAvailable(it)
        }
    }

    fun deviceDetached(device: UsbDevice) {
        fCurrentDevice?.let{
            if(it.first == device) {
                fCurrentDevice = null
                fListener.onInputDeviceUnavailable()
            }
        }
    }

    // Private implementation

    fun open(context: Context, device: UsbDevice) = if (device.manufacturerName == "Arduino LLC") {
        val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
        if (driver.ports.size > 0) {
            HandheldControls(context, driver.ports[0])
        } else {
            null
        }
    } else {
        null
    }

}