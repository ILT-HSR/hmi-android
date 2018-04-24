package ch.hsr.ifs.gcs

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

        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)

        fragmentHandler = FragmentHandler(this, map)

        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)

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
            } else if (it.device.manufacturerName.equals("FTDI")) {
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
                when(fragmentHandler?.activeFragment) {
                    FragmentType.MISSION_STATUSES_FRAGMENT, FragmentType.MISSION_RESULTS_FRAGMENT -> {
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                    }
                    FragmentType.NEEDS_FRAGMENT -> {
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEED_INSTRUCTION_FRAGMENT)
                    }
                    FragmentType.NEED_INSTRUCTION_FRAGMENT -> {
                        Log.d(TAG, "Start Mission Pressed")
                    }
                }
                leftButton.background = applicationContext.getDrawable(R.drawable.cancel_action)
            }
            HandheldControls.Button.DPAD_RIGHT -> {
                when(fragmentHandler?.activeFragment) {
                    FragmentType.MISSION_STATUSES_FRAGMENT -> {
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_RESULTS_FRAGMENT)
                        leftButton.background = applicationContext.getDrawable(R.drawable.refresh_mission)
                    }
                    FragmentType.MISSION_RESULTS_FRAGMENT -> {
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)
                    }
                }
            }
            HandheldControls.Button.DPAD_UP -> {
                (drone as? MAVLinkPlatform)?.arm()
            }
            HandheldControls.Button.DPAD_DOWN -> {
                (drone as? MAVLinkPlatform)?.disarm()
            }
            HandheldControls.Button.BTN_LEFT -> {
                when(fragmentHandler?.activeFragment) {
                    FragmentType.MISSION_STATUSES_FRAGMENT -> {
                        Log.d(TAG, "Cancel Mission Pressed")
                    }
                    FragmentType.MISSION_RESULTS_FRAGMENT -> {
                        Log.d(TAG, "Refresh Mission Pressed")
                    }
                    FragmentType.NEEDS_FRAGMENT -> {
                        val previousFragment = fragmentHandler!!.previousFragment
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
                        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)
                    }
                    FragmentType.NEED_INSTRUCTION_FRAGMENT -> {
                        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                        leftButton.background = applicationContext.getDrawable(R.drawable.cancel_action)
                    }
                }

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
