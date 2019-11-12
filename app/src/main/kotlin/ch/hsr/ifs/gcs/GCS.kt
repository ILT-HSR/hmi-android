package ch.hsr.ifs.gcs

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import ch.hsr.ifs.gcs.driver.PlatformModel
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.mission.Scheduler
import ch.hsr.ifs.gcs.mission.access.NeedManager
import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ifs.gcs.resource.ResourceManager
import ch.hsr.ifs.gcs.resource.ResourceNode
import java.nio.charset.Charset
import java.util.*

private const val RESOURCES_DIRECTORY = "resources"

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

    // Application implementation

    override fun onCreate() {
        super.onCreate()
        fContext = this

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        fResourceModel = ResourceModel()
        fPlatformModel = PlatformModel()
        fMainModel = MainModel()

        val channelType = preferences.getString(PREFERENCE_KEY_CHANNEL_TYPE, PREFERENCE_DEFAULT_CHANNEL_TYPE)
        val resourceFiles = assets.list(RESOURCES_DIRECTORY)?.mapNotNull {
            val charset = Charset.defaultCharset().name()
            Scanner(assets.open("$RESOURCES_DIRECTORY/$it"), charset).use { scn ->
                scn.useDelimiter("\\A").next()
            }
        } ?: emptyList()

        val resourceManagerConfiguration = ResourceManager.Parameters(
                resourceFiles,
                channelType!!,
                this,
                ResourceNode.Parameters(
                        "10.0.2.2",
                        2222
                ),
                this
        )

        fResourceManager = ResourceManager(resourceManagerConfiguration).apply {
            start()
        }

        fNeedManager = NeedManager(this).apply {
            onCreate(fResourceModel)
        }

        fMainModel.missions.observeForever {
            (it ?: emptyList()).forEach(fScheduler::launch)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        fMainModel.onDestroy()
        fNeedManager.onDestroy(fResourceModel)
        fResourceManager.stop()
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