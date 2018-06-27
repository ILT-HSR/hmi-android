package ch.hsr.ifs.gcs.ui

import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.driver.Input.Control
import ch.hsr.ifs.gcs.driver.access.InputProvider
import ch.hsr.ifs.gcs.mission.access.NeedProvider
import ch.hsr.ifs.gcs.resource.access.ResourceManager
import ch.hsr.ifs.gcs.support.geo.LocationService
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler
import ch.hsr.ifs.gcs.ui.fragments.FragmentHandler.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsListener
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesListener
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionListener
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsFragment
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsListener
import ch.hsr.ifs.gcs.ui.mission.need.NeedItemFactory
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

class MainActivity(
        missionResultsListener: MissionResultsListener = MissionResultsListener(),
        missionStatusesListener: MissionStatusesListener = MissionStatusesListener(),
        needsListener: NeedsListener = NeedsListener(),
        needInstructionListener: NeedInstructionListener = NeedInstructionListener()
) :
        AppCompatActivity(),
        Input.Listener,
        LocationService.OnLocationChangedListener,
        FragmentHandler,
        MissionResultsFragment.OnResultsFragmentChangedListener by missionResultsListener,
        MissionStatusesFragment.OnStatusesFragmentChangedListener by missionStatusesListener,
        NeedsFragment.OnNeedsFragmentChangedListener by needsListener,
        NeedInstructionFragment.OnNeedInstructionFragmentListener by needInstructionListener {

    private lateinit var fLocationService: LocationService
    private lateinit var fLocation: Location
    private var fActiveFragment = FragmentType.MISSION_RESULTS_FRAGMENT.fragment
    private var fPreviousFragment = FragmentType.MISSION_RESULTS_FRAGMENT.fragment

    val needItemFactory by lazy { NeedItemFactory(this) }
    val parameterItemFactory by lazy { ParameterItemFactory(this) }
    val resourceManager by lazy { ResourceManager(this) }
    val needProvider by lazy { NeedProvider(resourceManager) }

    var controls: Input? = null


    init {
        missionResultsListener.activity = this
        missionStatusesListener.activity = this
        needsListener.activity = this
        needInstructionListener.activity = this
    }

    // Activity implementation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_main)

        leftButton.background = applicationContext.getDrawable(R.drawable.abort_mission)

        performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.controller.setZoom(19.0)
        map.setBuiltInZoomControls(true)

        fLocationService = LocationService(this, this)

        InputProvider.instantiate(this)?.apply {
            controls = this
            addListener(this@MainActivity)
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

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        finish()
    }

    // Input.Handler implementation

    override fun onJoystick(control: Control, value: Byte) {
        Log.d("JOYSTICK", "Not implemented yet")
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

    // LocationService.OnLocationChangedListener implementation

    override fun onCurrentLocationChanged(location: Location) {
        if (!this::fLocation.isInitialized) {
            this.fLocation = location
            map.controller.setCenter(GeoPoint(location))
            map.invalidate()
        }
    }

    // FragmentHandler implementation

    override fun performFragmentTransaction(holderId: Int, fragmentType: FragmentHandler.FragmentType) {
        fPreviousFragment = fActiveFragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(holderId, fragmentType.fragment)
        transaction.commit()
        fActiveFragment = fragmentType.fragment
    }

    override fun performFragmentTransaction(holderId: Int, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(holderId, fragment)
        transaction.commit()
        fActiveFragment = fragment
    }

    override fun removeFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commit()
        fActiveFragment = fPreviousFragment
    }

}
