package ch.hsr.ifs.gcs.driver.access

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.input.HandheldControls
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException
import java.util.concurrent.Executors

class InputManager(private val fListener: Listener) {

    interface Listener {

        fun onInputDeviceAvailable(device: Input)

        fun onInputDeviceUnavailable()

    }

    private var fCurrentDevice: Pair<UsbDevice, Input>? = null
    private val fDeviceFilter: (UsbSerialDriver) -> Boolean = { it.device.manufacturerName == "Arduino LLC" && fCurrentDevice?.first != it.device }
    private val fDeviceScanner = Executors.newSingleThreadExecutor()

    fun start(context: Context) {
        fDeviceScanner.submit { scan(context) }
    }

    fun stop() {
        fDeviceScanner.shutdownNow()
    }

    fun deviceAttached(context: Context, device: UsbDevice) {
        if (fCurrentDevice != null) {
            return
        }
        UsbSerialProber.getDefaultProber().probeDevice(device)?.let {
            if (fDeviceFilter(it)) {
                fDeviceScanner.submit { probe(context, it) }
            }
        }
    }

    fun deviceDetached(device: UsbDevice) {
        fCurrentDevice?.let {
            if (it.first == device) {
                fCurrentDevice = null
                fListener.onInputDeviceUnavailable()
            }
        }
    }

    // Private implementation

    private fun probe(context: Context, driver: UsbSerialDriver) =
            if (driver.ports.size > 0) {
                try {
                    HandheldControls(context, driver.ports[0]).let {
                        fCurrentDevice = Pair(driver.device, it)
                        fListener.onInputDeviceAvailable(it)
                    }
                } catch (e: IOException) {

                }
            } else {
                null
            }

    private fun scan(context: Context) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(manager).filter(fDeviceFilter).forEach {
            probe(context, it)
        }
    }
}