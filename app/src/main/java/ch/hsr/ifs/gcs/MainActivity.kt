package ch.hsr.ifs.gcs

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import ch.hsr.ifs.gcs.driver.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.Platform
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.android.synthetic.main.activity_main.*
import me.drton.jmavlib.MAVLINK_SCHEMA_COMMON
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.newArmMessage
import me.drton.jmavlib.newDisarmMessage
import me.drton.jmavlib.newMAVLinkHeartbeat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var button: UsbSerialPort? = null
    private var drone: Platform? = null

    private val mExecutor = Executors.newSingleThreadExecutor()

    private var buttonIOManager: SerialInputOutputManager? = null

    private val buttonListener = object : SerialInputOutputManager.Listener {

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

        button_detected_field.text = "What is happening???"
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

        val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).forEach {
            if (it.device.manufacturerName.equals("Arduino (www.arduino.cc)")) {
                button = it.ports[0]
            } else if (it.device.manufacturerName.equals("FTDI")) {
                drone = MAVLinkPlatform.create(this, it.ports[0])
            }
        }

        button?.let { b ->
            mUsbManager.openDevice(b.driver.device)?.let {
                try {
                    b.open(it)
                    b.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                    button_detected_field.text = "Button connected"
                } catch (e: IOException) {
                    Log.e(TAG, "Error setting up button", e)
                }
            }
        }

        onDeviceStateChange()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()

        stopIoManagers()
        button?.let {
            try {
                it.close()
            } catch (e: IOException) {
            }
        }

        finish()
    }

    private fun startIoManagers() {
        button?.let {
            buttonIOManager = SerialInputOutputManager(button, buttonListener)
            mExecutor.submit(buttonIOManager)
        }
    }

    private fun stopIoManagers() {
        buttonIOManager?.stop()
        buttonIOManager = null
    }

    private fun onDeviceStateChange() {
        stopIoManagers()
        startIoManagers()
    }

    @SuppressLint("SetTextI18n")
    private fun updateReceivedData(data: ByteArray) {
        var message = ""
        data.forEach {
            message += String.format("0x%02x ", it)
        }

        if (data.contains(0x04)) {
            (drone as MAVLinkPlatform?)?.arm()
        } else if (data.contains(0x02)) {
            (drone as MAVLinkPlatform?)?.disarm()
        }
    }

}
