package ch.hsr.ifs.gcs.ui.fragments.needs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.ui.fragments.MenuFragmentID
import ch.hsr.ifs.gcs.ui.mission.need.NeedItem
import ch.hsr.ifs.gcs.ui.fragments.needinstructions.NeedInstructionFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_list.*
import kotlinx.android.synthetic.main.fragment_need_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [NeedsFragment.OnNeedsFragmentChangedListener] interface.
 */
class NeedsFragment : Fragment() {

    private var listener: OnNeedsFragmentChangedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNeedsFragmentChangedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnNeedsFragmentChangedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                layoutManager = LinearLayoutManager(context)
                adapter = NeedsRecyclerViewAdapter(
                        listener,
                        list,
                        context as MainActivity
                )
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (context as? MainActivity)?.let { ctx ->
            selectButton.setOnClickListener {
                with(ctx.showMenuFragment(MenuFragmentID.NEED_INSTRUCTION_FRAGMENT) as NeedInstructionFragment) {
                    need = (this@NeedsFragment.list.adapter as NeedsRecyclerViewAdapter).activeItem
                }
            }

            activity?.apply {
                leftButton.background = ctx.applicationContext.getDrawable(R.drawable.cancel_action)
                leftButton.setOnClickListener {
                    ctx.showMenuFragment(MenuFragmentID.MISSION_STATUSES_FRAGMENT)
                    leftButton.background = ctx.applicationContext.getDrawable(R.drawable.abort_mission)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        listener?.refreshNeedsMapView()
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
         * Called when a [Need] is clicked in the [RecyclerView]. Implementation
         * defines what to do with the provided [item].
         * @param item The item that has been clicked.
         */
        fun onNeedItemChanged(item: NeedItem)

        /**
         * Called when fragment is attached to its parent. Implementation should redraw the mapView
         * according to the use case of this fragment.
         */
        fun refreshNeedsMapView()

    }

}
