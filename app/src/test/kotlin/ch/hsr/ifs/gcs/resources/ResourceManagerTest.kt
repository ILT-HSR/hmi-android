package ch.hsr.ifs.gcs.resources

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildContentProvider
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ContentProviderController

@RunWith(RobolectricTestRunner::class)
class ResourceManagerTest {

    private lateinit var contentProvider: ContentProviderController<BuiltinResourceProvider>

    @Before
    fun initializeResourceManager() {
        contentProvider = buildContentProvider(BuiltinResourceProvider::class.java).create()
    }

    @After
    fun resetResourceManager() {
        contentProvider.shutdown()
        ResourceManager.reset()
    }

    @Test
    fun `ResourceManager has no available resources after startup`() {
        assertThat(ResourceManager.availableResources, empty())
    }

    @Test
    fun `ResourceManager contains the builtin resources after startup`() {
        assertThat(ResourceManager.allResources, hasSize(1))
    }

    @Ignore
    @Test
    fun `After marking a resource as available, the ResourceManager has one more resource available`() {
        val originalNumberOfAvailableResources = ResourceManager.availableResources.size
        val resource = ResourceManager.allResources.take(1)
        ResourceManager.acquire(resource[0])

    }

    @Test
    fun `After acquiring a resource, the ResourceManager has one less resource available`() {
        val originalNumberOfAvailableResources = ResourceManager.availableResources.size

    }
}