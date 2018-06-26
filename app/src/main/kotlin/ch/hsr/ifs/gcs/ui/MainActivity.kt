package ch.hsr.ifs.gcs.ui

import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.driver.access.InputProvider
import ch.hsr.ifs.gcs.support.geo.LocationService
import ch.hsr.ifs.gcs.resource.access.ResourceManager
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.mission.need.NeedItemFactory
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity(), Input.Listener, LocationService.OnLocationChangedListener {

    var fragmentHandler: FragmentHandler? = null
    var controls: Input? = null
    private var locationService: LocationService? = null

    private var location: Location? = null

    private lateinit var fNeedItemFactory: NeedItemFactory
    private lateinit var fParameterItemFactory: ParameterItemFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        fNeedItemFactory = NeedItemFactory(this)
        fParameterItemFactory = ParameterItemFactory(this)

        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)

        fragmentHandler = FragmentHandler(this, map)

        fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(19.0)
        map.setBuiltInZoomControls(true)

        locationService = LocationService(this, this)

        InputProvider.instantiate(this)?.apply {
            controls = this
            addListener(this@MainActivity)
        }

        ResourceManager.startScanning(this)
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

    override fun onButton(control: Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Control.ZOOM_IN -> {
                runOnUiThread {
                    map.controller.zoomIn()
                }
            }
            Control.ZOOM_OUT -> {
                runOnUiThread {
                    map.controller.zoomOut()
                }
            }

        }
    }

    override fun onJoystick(control: Control, value: Byte) {
        Log.d("JOYSTICK", "Not implemented yet")
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
        if (this.location == null) {
            this.location = location
            map.controller.setCenter(GeoPoint(location))
            map.invalidate()
        }
    }

    val needItemFactory get() = fNeedItemFactory

    val parameterItemFactory get() = fParameterItemFactory
}
