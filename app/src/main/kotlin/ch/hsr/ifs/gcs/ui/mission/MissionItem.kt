package ch.hsr.ifs.gcs.ui.mission

import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.support.color.createRandomColorArgb
import ch.hsr.ifs.gcs.ui.mission.need.NeedItemFactory
import org.osmdroid.views.overlay.Overlay

/**
 * This class wraps a concrete [Mission] for display in the UI.
 *
 * @param mission The concrete mission to wrap
 * @param color A color to associate with this item.
 *
 * @author IFS Institute for Software
 * @since 1.0.0
 */
class MissionItem(val mission: Mission) {

    private var fIsActive = false

    /**
     * Whether or not the item is currently selected in the UI
     *
     * since 1.0.0
     */
    val isActive get() = fIsActive

    /**
     * A user-friendly string to describe the current mission status
     */
    val status get() = mission.status

    /**
     * The map overlays associated with the underlying mission
     */
    val mapOverlays: Collection<Overlay> = emptyList()

    fun activate() {
        fIsActive = true
    }

    fun deactivate() {
        fIsActive = false
    }
}