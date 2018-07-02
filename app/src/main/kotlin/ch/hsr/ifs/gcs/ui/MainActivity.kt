package ch.hsr.ifs.gcs.ui

import android.arch.lifecycle.Observer
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.MainModel
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.R.drawable.abort_mission
import ch.hsr.ifs.gcs.R.layout.activity_main
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.support.geo.LocationService
import ch.hsr.ifs.gcs.ui.mission.MissionResultsFragment
import ch.hsr.ifs.gcs.ui.mission.MissionStatusesFragment
import ch.hsr.ifs.gcs.ui.mission.need.NeedInstructionFragment
import ch.hsr.ifs.gcs.ui.mission.need.NeedItemFactory
import ch.hsr.ifs.gcs.ui.mission.need.NeedsFragment
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource
import org.osmdroid.util.GeoPoint
import java.util.*

class MainActivity : AppCompatActivity(), Input.Listener, LocationService.OnLocationChangedListener {

    private lateinit var fLocationService: LocationService
    private lateinit var fLocation: Location
    private lateinit var fModel: MainModel

    private var fMenuFragment = MenuFragmentID.MISSION_STATUSES_FRAGMENT
    private var fMainFragment: Fragment? = null
    //private val fDeviceScanner = DeviceHandler()

    private lateinit var fParameterItemFactory: ParameterItemFactory
    private lateinit var fNeedItemFactory: NeedItemFactory

    val needItemFactory get() = fNeedItemFactory
    val parameterItemFactory get() = fParameterItemFactory

    fun showMainFragment(fragment: Fragment) {
        fMainFragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.mapholder, fragment)
                .commit()
    }

    fun hideMainFragment() {
        if (fMainFragment != null) {
            supportFragmentManager.beginTransaction()
                    .remove(fMainFragment)
                    .commit()
            fMainFragment = null
        }
    }

    // Activity implementation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(activity_main)

        BingMapTileSource.retrieveBingKey(this)
        val tileSource = BingMapTileSource(Locale.getDefault().displayName)
        tileSource.style = BingMapTileSource.IMAGERYSET_AERIAL
        map.setTileSource(tileSource)
        map.controller.setZoom(18.0)
        map.setBuiltInZoomControls(true)

        showMenuFragment(fMenuFragment)

        leftButton.background = applicationContext.getDrawable(abort_mission)

        fLocationService = LocationService(this, this)
        fParameterItemFactory = ParameterItemFactory(this)
        fNeedItemFactory = NeedItemFactory(this)

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

    override fun onResume() {
        super.onResume()
        map.onResume()
        //fDeviceScanner.start(this)
        showMenuFragment(fMenuFragment)

        fModel = (application as GCS).mainModel
        fModel.activeMenuFragment.observe(this, Observer {
            if (it == null) {
                showMenuFragment(MenuFragmentID.MISSION_STATUSES_FRAGMENT)
            } else {
                showMenuFragment(it)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        finish()
    }

    override fun onDestroy() {
        //fDeviceScanner.stop()
        super.onDestroy()
    }

    // Input.Handler implementation

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

    // LocationService.OnLocationChangedListener implementation

    override fun onCurrentLocationChanged(location: Location) {
        if (!this::fLocation.isInitialized) {
            this.fLocation = location
            map.controller.setCenter(GeoPoint(location))
            map.invalidate()
        }
    }

    // Private implementation

    private fun showMenuFragment(id: MenuFragmentID) =
            with(supportFragmentManager.findFragmentByTag(id.name) ?: createFragment(id)) {
                fMenuFragment = id
                supportFragmentManager.beginTransaction()
                        .replace(R.id.menuholder, this)
                        .commit()
                this
            }

    private fun createFragment(id: MenuFragmentID): Fragment = when (id) {
        MenuFragmentID.MISSION_RESULTS_FRAGMENT -> MissionResultsFragment()
        MenuFragmentID.MISSION_STATUSES_FRAGMENT -> MissionStatusesFragment()
        MenuFragmentID.NEEDS_FRAGMENT -> NeedsFragment()
        MenuFragmentID.NEED_INSTRUCTION_FRAGMENT -> NeedInstructionFragment()
    }

}
