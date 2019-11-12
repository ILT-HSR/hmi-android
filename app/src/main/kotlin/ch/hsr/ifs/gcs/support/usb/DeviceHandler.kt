package ch.hsr.ifs.gcs.support.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import ch.hsr.ifs.gcs.GCS

class DeviceHandler : BroadcastReceiver() {

    companion object {
        private const val LOG_TAG = "DeviceHandler"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent == null || context == null) {
            return
        }

        when(intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let { handleDeviceAttached(context, it) }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                device?.let { handleDeviceDetached(context, it) }
            }
        }
    }

    private fun handleDeviceAttached(context: Context, device: UsbDevice) {
        Log.i(LOG_TAG, "Device attached")
    }

    private fun handleDeviceDetached(context: Context, device: UsbDevice) {
        Log.i(LOG_TAG, "Device detached")
    }

}