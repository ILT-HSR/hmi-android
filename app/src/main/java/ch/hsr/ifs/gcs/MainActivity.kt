package ch.hsr.ifs.gcs

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.api.IMapController



class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var button: UsbSerialPort? = null
    private var drone: UsbSerialPort? = null

    private val mExecutor = Executors.newFixedThreadPool(3)

    private var buttonIOManager: SerialInputOutputManager? = null
    private var droneIOManager: SerialInputOutputManager? = null

    val mavlinkStream = MAVLinkStream(MAVLINK_SCHEMA_COMMON, object : ByteChannel {
        override fun write(data: ByteBuffer): Int {
            val array = ByteArray(data.remaining())
            data.get(array)
            return drone?.write(array, 100) ?: 0
        }

        override fun isOpen() = true
        override fun close() = Unit
        override fun read(p0: ByteBuffer?) = 0
    })

    private val buttonListener = object : SerialInputOutputManager.Listener {

        override fun onRunError(e: Exception) {
            Log.d(TAG, "Runner stopped.")
        }

        override fun onNewData(data: ByteArray) {
            Thread(Runnable {
                this@MainActivity.runOnUiThread({
                    this@MainActivity.updateReceivedData(button!!, data)
                })
            }).start()
        }
    }

    private val droneListener = object : SerialInputOutputManager.Listener {

        override fun onRunError(e: Exception) {
            Log.d(TAG, "Runner stopped.")
        }

        override fun onNewData(data: ByteArray) {
            Thread(Runnable {
                this@MainActivity.runOnUiThread({
                    this@MainActivity.updateReceivedData(drone!!, data)
                })
            }).start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Remove notification bar
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        map.setTileSource(TileSourceFactory.MAPNIK)
        val mapController = map.controller
        mapController.setZoom(18.0)
        //TODO: Get location from device to find center coordinates
        val startPoint = GeoPoint(47.223231, 8.816547)
        mapController.setCenter(startPoint)
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

        val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).forEach {
            if (it.device.manufacturerName.equals("Arduino (www.arduino.cc)")) {
                button = it.ports[0]
            } else if (it.device.manufacturerName.equals("FTDI")) {
                drone = it.ports[0]
            }
        }

        button?.let { b ->
            mUsbManager.openDevice(b.driver.device)?.let {
                try {
                    b.open(it)
                    b.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                    Log.d("BUTTON","Button Connected")
                } catch (e: IOException) {
                    Log.e(TAG, "Error setting up button", e)
                }
            }
        }

        drone?.let { v ->
            mUsbManager.openDevice(v.driver.device)?.let {
                try {
                    v.open(it)
                    v.setParameters(57600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                    Log.d("DRONE","Drone Connected")
                } catch (e: IOException) {
                    Log.e(TAG, "Error setting up drone", e)
                }
            }
        }

        mExecutor.submit {
            val heartbeat = newMAVLinkHeartbeat()
            while (true) {
                mavlinkStream.write(heartbeat)
                Thread.sleep(1000)
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

        drone?.let {
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

        drone?.let {
            droneIOManager = SerialInputOutputManager(drone, droneListener)
            mExecutor.submit(droneIOManager)
        }
    }

    private fun stopIoManagers() {
        buttonIOManager?.stop()
        buttonIOManager = null
        droneIOManager?.stop()
        droneIOManager = null
    }

    private fun onDeviceStateChange() {
        stopIoManagers()
        startIoManagers()
    }

    @SuppressLint("SetTextI18n")
    private fun updateReceivedData(port: UsbSerialPort, data: ByteArray) {
        var message = ""
        data.forEach {
            message += String.format("0x%02x ", it)
        }
        when (port) {
            button -> {
                Log.d("BUTTON","read ${data.size} bytes: $message")
                if (data.contains(0x04)) {
                    mavlinkStream.write(newArmMessage())
                } else if (data.contains(0x02)) {
                    mavlinkStream.write(newDisarmMessage())
                }
            }
            drone -> Log.d("DRONE","read ${data.size} bytes: $message")
        }
    }

}
