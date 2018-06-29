package ch.hsr.ifs.gcs

import android.app.Application
import android.util.Log
import ch.hsr.ifs.gcs.driver.NewPlatformAvailable
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.PlatformModel
import ch.hsr.ifs.gcs.driver.access.PlatformManager
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager

class GCS : Application(), ResourceManager.Listener, PlatformManager.Listener {

    private val fResourceModel = ResourceModel()
    private val fPlatformModel = PlatformModel()

    private lateinit var fResourceManager: ResourceManager
    private lateinit var fNeedManager: NeedManager
    private lateinit var fPlatformManager: PlatformManager

    companion object {
        private val LOG_TAG = GCS::class.simpleName
    }

    // Application implementation

    override fun onCreate() {
        super.onCreate()
        fResourceManager = ResourceManager(this)
        fNeedManager = NeedManager(fResourceModel)
        fPlatformManager = PlatformManager(this)

        fResourceManager.onCreate(this, fPlatformModel)
        fPlatformManager.start(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.i(LOG_TAG, "Application terminating")

        fPlatformManager.stop()
        fResourceManager.onDestroy(fPlatformModel)
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

}