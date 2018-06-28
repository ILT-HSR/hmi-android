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
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.ui.MainModel
import ch.hsr.ifs.gcs.ui.NeedOverviewRequested
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.view.*


/**
 * A fragment representing a list of mission status items combined with a button to add
 * additional needs. The context containing this fragment must implement the
 * [MissionStatusesFragment.OnStatusesFragmentChangedListener] interface.
 */
class MissionStatusesFragment : Fragment() {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: MissionStatusesRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_missionstatuses_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                fAdapter =  MissionStatusesRecyclerViewAdapter(this, context as MainActivity)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fModel = ViewModelProviders.of(activity!!)[MainModel::class.java]
        fModel.activeMissions.observe(this, Observer {
            fAdapter.missions = it ?: emptyList()
        })
        fModel.availableNeeds.observe(this, Observer {
            statusesAddButton.isEnabled = it != null && it.isNotEmpty()
        })

        activity?.apply {
            statusesAddButton.setOnClickListener{
                fModel.event(NeedOverviewRequested())
            }
            leftButton?.background = applicationContext?.getDrawable(R.drawable.cancel_action)
            leftButton?.setOnClickListener{
                // TODO: Implement mission cancelation
            }
        }
    }

}
