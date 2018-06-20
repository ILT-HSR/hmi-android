package ch.hsr.ifs.gcs.ui.fragments.missionstatuses

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.data.Missions
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needs.NeedsManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.view.*

/**
 * A fragment representing a list of mission status items combined with a button to add
 * additional needs. The context containing this fragment must implement the
 * [MissionStatusesFragment.OnStatusesFragmentChangedListener] interface.
 */
class MissionStatusesFragment : Fragment(), NeedsManager.OnNeedsAvailabilityChangedListener {

    companion object {
        private val LOG_TAG = MissionStatusesFragment::class.java.simpleName
    }

    private var listener: OnStatusesFragmentChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            listener = context.fragmentHandler?.missionStatusesListener
            NeedsManager += this
        } else {
            throw RuntimeException(context.toString() + " must implement OnStatusesFragmentChangedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_missionstatuses_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                layoutManager = LinearLayoutManager(context)
                adapter = MissionStatusesRecyclerViewAdapter(
                        listener,
                        this,
                        context as MainActivity
                )
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusesAddButton.setOnClickListener {
            val context = context
            if (context is MainActivity) {
                context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
            }
            activity?.leftButton?.background = context?.applicationContext?.getDrawable(R.drawable.cancel_action)
        }
        statusesAddButton.isEnabled = !NeedsManager.needs.isEmpty()
        activity?.leftButton?.setOnClickListener {
            Log.d(LOG_TAG, "Cancel Mission Pressed")
        }
    }

    override fun onStart() {
        super.onStart()
        listener?.refreshStatusesMapView()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        NeedsManager -= this
    }

    override fun onNeedsAvailabilityChanged(availability: Boolean) {
        (context as MainActivity).runOnUiThread {
            statusesAddButton.isEnabled = availability
        }
    }

    /**
     * Defines functions to be overwritten by the context making use of this fragment.
     */
    interface OnStatusesFragmentChangedListener {

        /**
         * Called when a [Missions.Item] is clicked in the [RecyclerView]. Implementation
         * defines what to do with the provided [item].
         * @param item The item that has been clicked.
         */
        fun onStatusItemChanged(item: Missions.Item?)

        /**
         * Called when fragment is attached to its parent. Implementation should redraw the mapView
         * according to the use case of this fragment.
         */
        fun refreshStatusesMapView()

    }

}
