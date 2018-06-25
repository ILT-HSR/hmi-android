package ch.hsr.ifs.gcs.ui.mission

import org.osmdroid.views.overlay.Overlay

object Results {

    private val fItems = mutableListOf<Item>()

    val size: Int
        get() = fItems.size

    class Item {

        var isSelected = false

        val mapOverlays: Collection<Overlay> = emptyList()

        val color: Int = 0

    }

    fun forEach(block: (Item) -> Unit) {
        fItems.forEach(block)
    }

    operator fun get(index: Int) = fItems[index]

    fun isNotEmpty() = fItems.isNotEmpty()

    fun indexOf(item: Item) = fItems.indexOf(item)


}
