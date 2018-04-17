package ifs.hsr.ch.gcs

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.IOException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var sPort: UsbSerialPort? = null

    private val mExecutor = Executors.newSingleThreadExecutor()

    private var mSerialIoManager: SerialInputOutputManager? = null

    private val mListener = object : SerialInputOutputManager.Listener {

        override fun onRunError(e: Exception) {
            Log.d(TAG, "Runner stopped.")
        }

        override fun onNewData(data: ByteArray) {
            Thread(Runnable {
                this@MainActivity.runOnUiThread({
                    this@MainActivity.updateReceivedData(data)
                })
            }).start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        map.setTileSource(TileSourceFactory.MAPNIK)

        textView.text = "What is happening???"
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

        val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (mUsbManager != null && UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).size > 0) {
            val driver = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).get(0)
            if (driver != null && driver.ports.size > 0) {
                sPort = driver.ports.get(0)
            }
        }

        Log.d(TAG, "Resumed, port=$sPort")
        if (sPort == null) {
            textView.text = "No serial device."
        } else {
            val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
            val connection = usbManager.openDevice(sPort!!.getDriver().device)
            if (connection == null) {
                textView.text = "Opening device failed"
                return
            }
            try {
                sPort!!.open(connection)
                sPort!!.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            } catch (e: IOException) {
                Log.e(TAG, "Error setting up device: " + e.message, e)
                textView.text = "Error opening device: " + e.message
                try {
                    sPort!!.close()
                } catch (e2: IOException) {
                    // Ignore.
                }
                sPort = null
                return
            }
            textView.text = "Serial device: " + sPort!!::class.java.simpleName
        }
        onDeviceStateChange()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()

        stopIoManager()
        if (sPort != null) {
            try {
                sPort!!.close()
            } catch (e: IOException) {
                // Ignore.
            }
            sPort = null
        }
        finish()
    }

    private fun startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..")
            mSerialIoManager = SerialInputOutputManager(sPort, mListener)
            mExecutor.submit(mSerialIoManager)
        }
    }

    private fun stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..")
            mSerialIoManager!!.stop()
            mSerialIoManager = null
        }
    }

    private fun onDeviceStateChange() {
        stopIoManager()
        startIoManager()
    }

    private fun updateReceivedData(data: ByteArray) {
        var message = ""
        data.forEach {
            message += String.format("0x%02x ", it)
        }
        textView.text = "read ${data.size} bytes: $message"
    }

    fun show(context: Context, port: UsbSerialPort) {
        sPort = port
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
        context.startActivity(intent)
    }

}
