package ch.hsr.ifs.gcs.ui.fragments.missionresults

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

import ch.hsr.ifs.gcs.ui.dummydata.MissionResultsDummyContent
import ch.hsr.ifs.gcs.ui.dummydata.MissionResultsDummyContent.MissionResultDummyItem
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.view.*

/**
 * A fragment representing a list of mission result items combined with a button to add
 * additional needs. The context containing this fragment must implement the
 * [MissionResultsFragment.OnResultsFragmentChangedListener] interface.
 */
class MissionResultsFragment : Fragment() {

    private val TAG = MissionResultsFragment::class.java.simpleName

    private var listener: OnResultsFragmentChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            listener = context.fragmentHandler!!.missionResultsListener
        } else {
            throw RuntimeException(context.toString() + " must implement OnResultsFragmentChangedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_missionresults_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                layoutManager = LinearLayoutManager(context)
                adapter = MissionResultsRecyclerViewAdapter(MissionResultsDummyContent.MISSION_RESULT_ITEMS, listener)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        resultsAddButton.setOnClickListener {
            val context = context
            if(context is MainActivity) {
                context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
            }
            activity.leftButton.background = context.applicationContext.getDrawable(R.drawable.cancel_action)
        }
        activity.leftButton.setOnClickListener {
            Log.d(TAG, "Refresh Mission Pressed")
        }
    }

    override fun onStart() {
        super.onStart()
        listener?.refreshResultsMapView(MissionResultsDummyContent.MISSION_RESULT_ITEMS)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Defines functions to be overwritten by the context making use of this fragment.
     */
    interface OnResultsFragmentChangedListener {

        /**
         * Called when a [MissionResultDummyItem] is clicked in the [RecyclerView]. Implementation
         * defines what to do with the provided [item].
         * @param item The item that has been clicked.
         */
        fun onResultItemChanged(item: MissionResultDummyItem?)

        /**
         * Called when fragment is attached to its parent. Implementation should redraw the mapView
         * according to the use case of this fragment.
         * @param items The list of all items of the fragment.
         */
        fun refreshResultsMapView(items: List<MissionResultDummyItem>)

    }

}
