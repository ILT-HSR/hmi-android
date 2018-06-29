package ch.hsr.ifs.gcs.ui.mission

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.MainModel
import ch.hsr.ifs.gcs.MissionOverviewRequested
import ch.hsr.ifs.gcs.NeedOverviewRequested
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.driver.Input
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.view.*

/**
 * A fragment representing a list of mission result items combined with a button to add
 * additional needs. The context containing this fragment must implement the
 * [MissionResultsFragment.OnResultsFragmentChangedListener] interface.
 */
class MissionResultsFragment : Fragment(), Input.Listener {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: MissionResultsRecyclerViewAdapter

    private var fControls: Input? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_missionresults_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                fAdapter = MissionResultsRecyclerViewAdapter(this)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fModel = ViewModelProviders.of(activity!!)[MainModel::class.java]
        fModel.missionResults.observe(this, Observer {
            fAdapter.results = it ?: emptyList()
        })
        fModel.availableNeeds.observe(this, Observer {
            resultsAddButton.isEnabled = it != null && it.isNotEmpty()
        })

        activity?.apply {
            resultsAddButton.setOnClickListener {
                fModel.event(NeedOverviewRequested())
            }
            leftButton?.background = context?.applicationContext?.getDrawable(R.drawable.cancel_action)
            // TODO: Implement mission cancellation
            fControls = fModel.getInputControls(this)
        }
        fControls?.addListener(this)
    }

    //Input.Listener implementation

    override fun onButton(control: Input.Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Input.Control.DPAD_UP -> {
                fAdapter.activatePreviousItem()
            }
            Input.Control.DPAD_DOWN -> {
                fAdapter.activateNextItem()
            }
            Input.Control.DPAD_LEFT -> {
                fModel.event(MissionOverviewRequested())
                fControls?.removeListener(this)
            }
            Input.Control.NEED_START -> {
                fModel.event(NeedOverviewRequested())
                fControls?.removeListener(this)
            }
        }
    }
}
