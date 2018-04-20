package ch.hsr.ifs.gcs.ui.fragments.needs

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.View
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

class NeedsListener(val activity: Activity, val map: MapView) : NeedsFragment.OnNeedsFragmentChangedListener {

    override fun onListFragmentInteraction(item: NeedsDummyContent.NeedDummyItem?) {
        val transaction = (activity as FragmentActivity).supportFragmentManager.beginTransaction()
        transaction.replace(R.id.menuholder, NeedInstructionFragment())
        transaction.addToBackStack(null)
        transaction.commit()
        (activity as MainActivity).leftButton.visibility = View.INVISIBLE
    }

    override fun refreshNeedsMapView() {
        activity.runOnUiThread({
            map.overlays.clear()
            map.invalidate()
        })
    }

}