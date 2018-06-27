package ch.hsr.ifs.gcs.driver.access

import android.content.Context
import android.hardware.usb.UsbDevice
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.input.HandheldControls
import ch.hsr.ifs.gcs.support.usb.DeviceScanner
import com.hoho.android.usbserial.driver.UsbSerialProber

class InputProvider(private val fScanner: DeviceScanner) : DeviceScanner.Listener {

    interface Listener {

        fun onInputDeviceAvailable(device: Input)

    }

    private val fListeners = mutableListOf<Listener>()
    private var fDevice: Input? = null

    init {
        fScanner.addListener(this)
    }

    fun addListener(listener: Listener) = synchronized(fListeners) {
        fListeners += listener
    }

    fun removeListener(listener: Listener) = synchronized(fListeners) {
        fListeners -= listener
    }

    operator fun get(context: Context): Input? = synchronized(fScanner.devices) {
        if(fDevice == null) {
            fDevice = fScanner.devices.map { open(context, it) }.firstOrNull()
        }
        fDevice
    }

    // DeviceScanner.Listener implementation

    override fun onNewDeviceFound(context: Context, device: UsbDevice) {
        if(fDevice == null) {
            open(context, device)?.let { dev ->
                fDevice = dev
                fListeners.forEach { it.onInputDeviceAvailable(dev) }
                fScanner.removeListener(this)
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