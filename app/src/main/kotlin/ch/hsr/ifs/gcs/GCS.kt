package ch.hsr.ifs.gcs

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.driver.*
import ch.hsr.ifs.gcs.driver.access.InputManager
import ch.hsr.ifs.gcs.driver.access.PlatformManager
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Scheduler
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager
import ch.hsr.ifs.gcs.ui.SettingsActivity

class GCS : Application(), ResourceManager.Listener, PlatformManager.Listener, NeedManager.Listener, InputManager.Listener {

    companion object {
        private lateinit var fContext: Context

        val context get() = fContext
    }

    private lateinit var fResourceModel: ResourceModel
    private lateinit var fPlatformModel: PlatformModel
    private lateinit var fMainModel: MainModel

    private lateinit var fResourceManager: ResourceManager
    private lateinit var fNeedManager: NeedManager
    private lateinit var fPlatformManager: PlatformManager
    private lateinit var fInputManager: InputManager

    private val fScheduler = Scheduler()

    val mainModel get() = fMainModel
    val platformManager get() = fPlatformManager
    val inputManager get() = fInputManager


    // Application implementation

    override fun onCreate() {
        super.onCreate()
        fContext = this

        fResourceModel = ResourceModel()
        fPlatformModel = PlatformModel()
        fMainModel = MainModel()

        fResourceManager = ResourceManager(this)
        fNeedManager = NeedManager(this)
        fPlatformManager = PlatformManager(this)
        fInputManager = InputManager(this)

        fResourceManager.onCreate(this, fPlatformModel)
        fNeedManager.onCreate(fResourceModel)
        fPlatformManager.start(this)
        fInputManager.start(this)

        fMainModel.activeMissions.observeForever {
            (it ?: emptyList()).forEach(fScheduler::launch)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean(PREFERENCE_KEY_ENABLE_NULL_PLATFORM, false)) {
            fPlatformModel.submit(NewPlatformAvailable(NullPlatform()))
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        fInputManager.stop()
        fPlatformManager.stop()
        fNeedManager.onDestroy(fResourceModel)
        fResourceManager.onDestroy(fPlatformModel)
        fMainModel.onDestroy()
    }

    // ResourceManager.Listener implementation

    override fun onNewResourceAvailable(resource: Resource) {
        fResourceModel.submit(NewResourceAvailable(resource))
    }

    override fun onResourceAcquired(resource: Resource) {
    }

    override fun onResourceUnavailable(resource: Resource) {
    }

    // PlatformManager.Listener implementation

    override fun onNewPlatformAvailable(platform: Platform) {
        fPlatformModel.submit(NewPlatformAvailable(platform))
    }

    // NeedManager.Listener implementation

    override fun onNewNeedAvailable(need: Need) {
        fMainModel.submit(NeedAvailable(need))
    }

    // InputManager.Listener implementation

    override fun onInputDeviceAvailable(device: Input) {
        fMainModel.submit(InputDeviceAvailable(device))
    }

    override fun onInputDeviceUnavailable() {
        fMainModel.submit(InputDeviceUnavailable)
    }

}