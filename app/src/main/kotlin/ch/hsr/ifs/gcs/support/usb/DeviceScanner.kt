package ch.hsr.ifs.gcs.support.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DeviceScanner : BroadcastReceiver() {

    interface Listener {

        fun onNewDeviceFound(context: Context, device: UsbDevice)

    }

    companion object {

        private const val PERMISSION_USB_DEVICE = "ch.hsr.ifs.gcs.permission.USB_DEVICE"

        private val LOG_TAG = DeviceScanner::class.simpleName

    }

    private lateinit var fContext: Context
    private val fDevicePermissionIntent by lazy { PendingIntent.getBroadcast(fContext, 0, Intent(PERMISSION_USB_DEVICE), 0) }
    private val fScanRunner = Executors.newSingleThreadScheduledExecutor()
    private val fListeners = mutableListOf<Listener>()

    val devices = mutableListOf<UsbDevice>()

    fun start(context: Context) {
        fContext = context
        val intentFilter = IntentFilter(PERMISSION_USB_DEVICE)
        fContext.registerReceiver(this, intentFilter)
        fScanRunner.scheduleAtFixedRate(this::scan, 0, 100, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        fScanRunner.shutdownNow()
        fContext.unregisterReceiver(this)
    }

    fun addListener(listener: Listener) = synchronized(fListeners) {
        fListeners += listener

    }
    fun removeListener(listener: Listener) = synchronized(fListeners) {
        fListeners -= listener
    }

    // BroadcastReceiver implementation

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(LOG_TAG, "onReceive $intent")
        if (intent.action == PERMISSION_USB_DEVICE) {
            val parcelableExtra = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            Log.i(LOG_TAG, "onReceive $parcelableExtra")
            synchronized(this) {
                Log.i(LOG_TAG, "onReceive: ${intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)}")
                parcelableExtra?.let { dev ->
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        devices += dev
                        Log.i(LOG_TAG, "device '$dev'")
                        synchronized(fListeners) {
                            fListeners.forEach { it.onNewDeviceFound(context, dev) }
                            Log.i(LOG_TAG, "listeners: '$fListeners'")
                        }
                    } else {
                        Log.e(LOG_TAG, "Permission denied for '$dev'")
                    }
                }
            }
        }
    }

    // Private implementation

    private fun scan() {
        val usbManager = fContext.getSystemService(Context.USB_SERVICE) as UsbManager
        usbManager.deviceList.forEach {
            if(!usbManager.hasPermission(it.value)) {
                usbManager.requestPermission(it.value, fDevicePermissionIntent)
            }
        }
    }

}