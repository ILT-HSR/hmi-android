package ch.hsr.ifs.gcs.driver

import android.content.Context
import android.hardware.usb.UsbManager
import ch.hsr.ifs.gcs.driver.input.HandheldControls
import com.hoho.android.usbserial.driver.UsbSerialProber

object InputProvider {

    fun instantiate(context: Context): Input? =
            with(context.getSystemService(Context.USB_SERVICE) as UsbManager) {
                UsbSerialProber.getDefaultProber().findAllDrivers(this).forEach {
                    if (it.device.manufacturerName.equals("Arduino LLC")) {
                        return@with HandheldControls(context, it.ports[0])
                    }
                }
                null
            }

}