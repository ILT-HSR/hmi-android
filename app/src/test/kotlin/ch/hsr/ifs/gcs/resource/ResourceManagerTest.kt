package ch.hsr.ifs.gcs.resource

import ch.hsr.ifs.gcs.resource.access.ResourceManager
import ch.hsr.ifs.gcs.resource.internal.BuiltinResourceProvider
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
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
        for (resource in ResourceManager.allResources) {
            resource.markAs(Resource.Status.UNAVAILABLE)
        }
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

    @Test
    fun `After marking a resource as available, the ResourceManager has one more resource available`() {
        val originalNumberOfAvailableResources = ResourceManager.availableResources.size
        val resource = ResourceManager.allResources.take(1)[0]
        resource.markAs(Resource.Status.AVAILABLE)

        assertThat(ResourceManager.availableResources, hasSize(originalNumberOfAvailableResources + 1))
    }

    @Test
    fun `After acquiring a resource, the ResourceManager has one less resource available`() {
        val resource = ResourceManager.allResources.take(1)[0]
        resource.markAs(Resource.Status.AVAILABLE)
        val originalNumberOfAvailableResources = ResourceManager.availableResources.size
        ResourceManager.acquire(resource)

        assertThat(ResourceManager.availableResources, hasSize(originalNumberOfAvailableResources - 1))
    }
}