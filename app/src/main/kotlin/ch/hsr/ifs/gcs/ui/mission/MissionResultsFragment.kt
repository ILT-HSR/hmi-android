package ch.hsr.ifs.gcs.ui.mission

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.*
import kotlinx.android.synthetic.main.fragment_missionresults_list.view.*

/**
 * A fragment representing a list of mission result items combined with a button to add
 * additional needs.
 */
class MissionResultsFragment : Fragment() {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: MissionResultsRecyclerViewAdapter

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
        fModel = (activity!!.application as GCS).mainModel
        fModel.missionResults.observe(this, Observer {
            fAdapter.results = it ?: emptyList()
        })
        fModel.availableNeeds.observe(this, Observer {
            resultsAddButton.isEnabled = it != null && it.isNotEmpty()
        })

        activity?.apply {
            resultsAddButton.setOnClickListener {
                fModel.submit(NeedOverviewRequested)
            }
            leftButton?.background = context?.applicationContext?.getDrawable(R.drawable.cancel_action)
            // TODO: Implement mission cancellation
        }
    }

}
