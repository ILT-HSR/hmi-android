package ch.hsr.ifs.gcs

import android.app.Application
import android.util.Log
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager

class GCS : Application(), ResourceManager.Listener {

    private val fResourceModel = ResourceModel()
    private val fNeedManager = NeedManager(fResourceModel)
    private lateinit var fResourceManager: ResourceManager

    companion object {
        private val LOG_TAG = GCS::class.simpleName
    }

    // Application implementation

    override fun onCreate() {
        super.onCreate()
        Log.i(LOG_TAG, "Application created")
        fResourceManager = ResourceManager(this, this)
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.i(LOG_TAG, "Application terminating")
    }

    // ResourceManager.Listener implementation

    override fun onNewResourceAvailable(resource: Resource) {
        fResourceModel.submit(NewResourceAvailable(resource))
    }

    override fun onResourceAcquired(resource: Resource) {
    }

    override fun onResourceUnavailable(resource: Resource) {
    }

}