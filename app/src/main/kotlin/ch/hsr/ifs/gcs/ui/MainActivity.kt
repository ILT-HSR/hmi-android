package ch.hsr.ifs.gcs.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.*
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
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*

const val PERMISSION_REQUEST_ID_MAP = 42

fun <T> Pair<Array<out T>, IntArray>.iterator(): Iterator<Pair<T, Int>> {
    val firstIterator = first.iterator()
    val secondIterator = second.iterator()

    return object : Iterator<Pair<T, Int>> {
        override fun hasNext() = firstIterator.hasNext() && secondIterator.hasNext()

        override fun next(): Pair<T, Int> {
            return Pair(firstIterator.next(), secondIterator.next())
        }

    }
}

class MainActivity : AppCompatActivity(), Input.Listener, LocationService.OnLocationChangedListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private data class PermissionState(val permission: String, val state: Int)

    private lateinit var fPreferences: SharedPreferences
    private lateinit var fLocationService: LocationService
    private lateinit var fLocation: Location
    private lateinit var fModel: MainModel

    private var fMenuFragment = MenuFragmentID.MISSION_STATUSES_FRAGMENT
    private var fMainFragment: Fragment? = null

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

        fPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setContentView(activity_main)

        requestLocationPermissions()

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
        fModel.activeInputDevice.observe(this, Observer {
            it?.removeListener(this)
            it?.addListener(this)
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
                map.controller.zoomIn()
            }
            Control.ZOOM_OUT -> {
                map.controller.zoomOut()
            }
        }
    }

    // ActivityCompat.OnRequestPermissionsResultCallback implementation

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val denied = mutableListOf<String>()
        Pair(permissions, grantResults).iterator().forEach { (p, s) ->
            if (s != PackageManager.PERMISSION_GRANTED) {
                denied += p
            }
        }

        if (denied.isNotEmpty()) {
            denied.forEach {
                Log.e(this@MainActivity::class.simpleName, "Denied required permission: '$it'")
            }
        } else {
            setupMap()
        }
    }

    // LocationService.OnLocationChangedListener implementation

    override fun onCurrentLocationChanged(location: Location) {
        if (!this::fLocation.isInitialized) {
            this.fLocation = location
            map.controller.animateTo(GeoPoint(location))
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

    private fun requestLocationPermissions() {
        val states = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .map { PermissionState(it, checkSelfPermission(it)) }

        if (states.all { (_, s) -> s == PackageManager.PERMISSION_GRANTED }) {
            setupMap()
        } else {
            val permissionsToRequest = states.asSequence()
                    .filter { (_, s) -> s != PackageManager.PERMISSION_GRANTED }
                    .map { (p, _) -> p }.toList().toTypedArray()
            requestPermissions(permissionsToRequest, PERMISSION_REQUEST_ID_MAP)
        }
    }

    private fun setupMap() {
        val mapSource = fPreferences.getString(PREFERENCE_KEY_MAP_SOURCE, PREFERENCE_VAL_MAP_SOURCE_OSM)

        when (mapSource) {
            PREFERENCE_VAL_MAP_SOURCE_OSM -> {
                map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                map.setBuiltInZoomControls(true)
            }
            PREFERENCE_VAL_MAP_SOURCE_BING -> {
                BingMapTileSource.retrieveBingKey(this)
                val tileSource = object : BingMapTileSource(Locale.getDefault().displayName) {
                    override fun getMaximumZoomLevel(): Int {
                        return 19
                    }
                }
                tileSource.style = BingMapTileSource.IMAGERYSET_AERIAL
                map.setTileSource(tileSource)
            }
            else -> Unit
        }

        map.controller.setZoom(18.0)
        map.setBuiltInZoomControls(true)

        MyLocationNewOverlay(GpsMyLocationProvider(this), map).apply {
            enableMyLocation()
            map.overlays += this
        }

        CompassOverlay(this, map).apply {
            enableCompass()
            map.overlays += this
        }
    }

}
