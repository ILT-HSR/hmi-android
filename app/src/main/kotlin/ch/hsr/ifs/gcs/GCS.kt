package ch.hsr.ifs.gcs

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.driver.*
import ch.hsr.ifs.gcs.driver.generic.platform.NullPlatform
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Scheduler
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager

class GCS : Application(), ResourceManager.Listener, NeedManager.Listener {

    companion object {
        private lateinit var fContext: Context

        val context get() = fContext
    }

    private lateinit var fResourceModel: ResourceModel
    private lateinit var fPlatformModel: PlatformModel
    private lateinit var fMainModel: MainModel

    private lateinit var fResourceManager: ResourceManager
    private lateinit var fNeedManager: NeedManager

    private val fScheduler = Scheduler()

    val mainModel get() = fMainModel
    val resourceManager get() = fResourceManager


    // Application implementation

    override fun onCreate() {
        super.onCreate()
        fContext = this

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        fResourceModel = ResourceModel()
        fPlatformModel = PlatformModel()
        fMainModel = MainModel()

        val channelType = preferences.getString(PREFERENCE_KEY_CHANNEL_TYPE, PREFERENCE_DEFAULT_CHANNEL_TYPE)

        fResourceManager = ResourceManager(this, channelType!!)
        fNeedManager = NeedManager(this)

        fResourceManager.onCreate(this)
        fNeedManager.onCreate(fResourceModel)

        fMainModel.missions.observeForever {
            (it ?: emptyList()).forEach(fScheduler::launch)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        fNeedManager.onDestroy(fResourceModel)
        fResourceManager.onDestroy()
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

    // NeedManager.Listener implementation

    override fun onNewNeedAvailable(need: Need) {
        fMainModel.submit(NeedAvailable(need))
    }

}