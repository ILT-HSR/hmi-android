package ch.hsr.ifs.gcs.ui.fragments.needs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R

import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent
import ch.hsr.ifs.gcs.ui.dummydata.NeedsDummyContent.NeedDummyItem
import ch.hsr.ifs.gcs.ui.fragments.missionresults.MissionResultsFragment
import ch.hsr.ifs.gcs.ui.fragments.missionstatuses.MissionStatusesFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_list.*
import kotlinx.android.synthetic.main.fragment_need_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [NeedsFragment.OnNeedsFragmentChangedListener] interface.
 */
class NeedsFragment : Fragment() {

    private var columnCount = 1

    private var listener: OnNeedsFragmentChangedListener? = null

    private var previousFragment = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            listener = context.fragmentHandler!!.needsListener
        } else {
            throw RuntimeException(context.toString() + " must implement OnNeedsFragmentChangedListener")
        }
        listener?.refreshNeedsMapView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = NeedsRecyclerViewAdapter(NeedsDummyContent.NEED_ITEMS, listener)
            }
        }
        previousFragment = arguments.getString("previous_fragment")
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelButton.setOnClickListener {
            val transaction = activity.supportFragmentManager.beginTransaction()
            var returnFragment = when(previousFragment) {
                "mission_results" -> MissionResultsFragment()
                "mission_statuses" -> MissionStatusesFragment()
                else -> {
                    MissionResultsFragment()
                }
            }
            transaction.replace(R.id.menuholder, returnFragment)
            transaction.addToBackStack(null)
            transaction.commit()
            activity.leftButton.visibility = View.VISIBLE
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Defines functions to be overwritten by the context making use of this fragment.
     */
    interface OnNeedsFragmentChangedListener {

        /**
         * Called when a [NeedDummyItem] is clicked in the [RecyclerView]. Implementation
         * defines what to do with the provided [item].
         * @param item The item that has been clicked.
         */
        fun onListFragmentInteraction(item: NeedDummyItem?)

        /**
         * Called when fragment is attached to its parent. Implementation should redraw the mapView
         * according to the use case of this fragment.
         */
        fun refreshNeedsMapView()

    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) = NeedsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLUMN_COUNT, columnCount)
            }
        }

    }

}
