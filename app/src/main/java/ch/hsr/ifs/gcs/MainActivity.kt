package ch.hsr.ifs.gcs

import android.content.Context
import android.hardware.usb.UsbManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.internal.MAVLinkPlatformPixhawkPX4
import ch.hsr.ifs.gcs.input.HandheldControls
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.util.LocationService
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity(), HandheldControls.Listener, LocationService.OnLocationChangedListener {

    private val TAG = MainActivity::class.java.simpleName
    // private var menuVisible = true

    var fragmentHandler: FragmentHandler? = null
    var controls: HandheldControls? = null
    private var locationService: LocationService? = null

    private var drone: Platform? = null
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)

        fragmentHandler = FragmentHandler(this, map)

        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)

        map.setTileSource(TileSourceFactory.MAPNIK)
        val mapController = map.controller
        mapController.setZoom(19.0)
        map.setBuiltInZoomControls(true)

        locationService = LocationService(this, this)

        val mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager).forEach {
            if (it.device.manufacturerName.equals("Arduino LLC")) {
                controls = HandheldControls(this, it.ports[0])
                controls?.addListener(this)
            } else { // if (it.device.manufacturerName.equals("FTDI")) {
                drone = MAVLinkCommonPlatform.create(::MAVLinkPlatformPixhawkPX4, this, it.ports[0])
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
        when(button) {
            /*
            HandheldControls.Button.SHOW_MENU -> {
                Log.d(TAG, "Show Menu Pressed")
                runOnUiThread {
                    if(menuVisible) {
                        guideline.setGuidelinePercent(100f)
                    } else {
                        guideline.setGuidelinePercent(75f)
                    }
                    menuVisible = !menuVisible
                    mainLayout.invalidate()
                    map.invalidate()
                }
            }
            */
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

    override fun onCurrentLocationChanged(location: Location) {
        if(this.location == null) {
            this.location = location
            map.controller.setCenter(GeoPoint(location))
            map.invalidate()
        }
    }

}
