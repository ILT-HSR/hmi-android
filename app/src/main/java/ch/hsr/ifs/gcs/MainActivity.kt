package ch.hsr.ifs.gcs

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    var fragmentHandler: FragmentHandler? = null

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

        leftButton.background = applicationContext.getDrawable(R.drawable.ic_autorenew_black_24dp)

        fragmentHandler = FragmentHandler(this, map)

        fragmentHandler?.let {
            it.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)
        }

        map.setTileSource(TileSourceFactory.MAPNIK)
        val mapController = map.controller
        mapController.setZoom(18.0)
        //TODO: Get location from device to find center coordinates
        val startPoint = GeoPoint(47.223231, 8.816547)
        map.setBuiltInZoomControls(true)
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
                drone = MAVLinkCommonPlatform.create(this, it.ports[0])
            }
        }

        button?.let { b ->
            mUsbManager.openDevice(b.driver.device)?.let {
                try {
                    b.open(it)
                    b.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                    Log.d("BUTTON", "Button Connected")
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
            fragmentHandler?.let {
                it.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
            }
            leftButton.background = applicationContext.getDrawable(R.drawable.ic_cancel_black_24dp)
        } else if (data.contains(0x02)) {
            (drone as MAVLinkPlatform?)?.disarm()
            fragmentHandler?.let {
                it.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)
            }
            leftButton.background = applicationContext.getDrawable(R.drawable.ic_autorenew_black_24dp)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

}
