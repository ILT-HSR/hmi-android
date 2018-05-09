package ch.hsr.ifs.gcs

import android.content.Context
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.input.HandheldControls
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity(), HandheldControls.Listener {

    private val TAG = MainActivity::class.java.simpleName

    var fragmentHandler: FragmentHandler? = null

    private var controls: HandheldControls? = null
    private var drone: Platform? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        leftButton.background = applicationContext.getDrawable(R.drawable.ic_autorenew_black_24dp)

        fragmentHandler = FragmentHandler(this, map)

        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)

        map.setTileSource(TileSourceFactory.MAPNIK)
        val mapController = map.controller
        mapController.setZoom(18.0)
        //TODO: Get location from device to find center coordinates
        val startPoint = GeoPoint(47.223231, 8.816547)
        map.setBuiltInZoomControls(true)
        mapController.setCenter(startPoint)

        val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).forEach {
            if (it.device.manufacturerName.equals("Arduino LLC")) {
                controls = HandheldControls(this, this, it.ports[0])
            } else { // if (it.device.manufacturerName.equals("FTDI")) {
                drone = MAVLinkCommonPlatform.create(this, it.ports[0])
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        finish()
    }

    override fun onButton(button: HandheldControls.Button) {
        when (button) {
            HandheldControls.Button.DPAD_LEFT -> {
                fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                leftButton.background = applicationContext.getDrawable(R.drawable.ic_cancel_black_24dp)
            }
            HandheldControls.Button.DPAD_RIGHT -> {
                (drone as? MAVLinkCommonPlatform)?.arm()
                fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)
                leftButton.background = applicationContext.getDrawable(R.drawable.ic_autorenew_black_24dp)
            }
            HandheldControls.Button.DPAD_UP -> {
                (drone as? MAVLinkCommonPlatform)?.takeOff(AerialVehicle.Altitude(1.0))
            }
            HandheldControls.Button.DPAD_DOWN -> {
                (drone as? MAVLinkCommonPlatform)?.land()
            }
            HandheldControls.Button.BTN_NEED -> {
                (drone as? MAVLinkCommonPlatform)?.changeAltitude(AerialVehicle.Altitude(2.0)) // Move
                fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                leftButton.visibility = View.INVISIBLE
            }
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
