package ch.hsr.ifs.gcs

import android.app.Application
import android.arch.lifecycle.ViewModelProviders
import android.util.Log
import ch.hsr.ifs.gcs.driver.NewPlatformAvailable
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.PlatformModel
import ch.hsr.ifs.gcs.driver.access.PlatformManager
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager
import ch.hsr.ifs.gcs.ui.MainActivity

class GCS : Application(), ResourceManager.Listener, PlatformManager.Listener, NeedManager.Listener {

    private lateinit var fResourceModel: ResourceModel
    private lateinit var fPlatformModel: PlatformModel
    private lateinit var fMainModel: MainModel

    private lateinit var fResourceManager: ResourceManager
    private lateinit var fNeedManager: NeedManager
    private lateinit var fPlatformManager: PlatformManager

    val mainModel get() = fMainModel

    // Application implementation

    override fun onCreate() {
        super.onCreate()
        fResourceModel = ResourceModel()
        fPlatformModel = PlatformModel()
        fMainModel = MainModel()

        fResourceManager = ResourceManager(this)
        fNeedManager = NeedManager(this)
        fPlatformManager = PlatformManager(this)

        fResourceManager.onCreate(this, fPlatformModel)
        fNeedManager.onCreate(fResourceModel)
        fPlatformManager.start(this)
    }

    override fun onTerminate() {
        super.onTerminate()
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
        fMainModel.event(NeedAvailable(need))
    }

}