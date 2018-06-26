package ch.hsr.ifs.gcs.ui.mission

import android.content.Context
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.need.Need
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
class MissionItem(val mission: Mission, needItemFactory: NeedItemFactory, val color: Int = createRandomColorArgb()) {

    /**
     * A user-friendly string to describe the current mission status
     */
    val status get() = mission.status

    /**
     * The need-item for the need associated with the underlying mission
     */
    val need = needItemFactory.instantiate(mission.need)

    /**
     * Whether or not the item is currently selected in the UI
     */
    var isSelected = false

    /**
     * The map overlays associated with the underlying mission
     */
    val mapOverlays: Collection<Overlay> = emptyList()

}