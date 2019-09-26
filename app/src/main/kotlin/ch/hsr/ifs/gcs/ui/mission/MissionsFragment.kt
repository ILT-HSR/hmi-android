package ch.hsr.ifs.gcs.ui.mission

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.MainModel
import ch.hsr.ifs.gcs.NeedOverviewRequested
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.Result
import ch.hsr.ifs.gcs.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missions_list.*
import kotlinx.android.synthetic.main.fragment_missions_list.view.*


/**
 * A fragment representing a list of mission status items combined with a button to add
 * additional needs.
 */
class MissionsFragment : Fragment() {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: MissionsRecyclerViewAdapter
    private val fResults = mutableMapOf<Mission, Result>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_missions_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                fAdapter = MissionsRecyclerViewAdapter(context as MainActivity)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fModel = (activity!!.application as GCS).mainModel
        fModel.missions.observe(this, Observer {
            fAdapter.missions = it?.reversed() ?: emptyList()
        })
        fModel.availableNeeds.observe(this, Observer {
            statusesAddButton.isEnabled = it != null && it.isNotEmpty()
        })
        fModel.missionResults.observe(this, Observer { missions ->
            val items = fAdapter.items.filterNot(MissionItem::hasResult)
            missions.forEach { result ->
                items.find{ it.mission == result.mission }?.let {
                    it.result = result
                    it.draw()
                }
            }
        })

        activity?.apply {
            statusesAddButton.setOnClickListener {
                fModel.submit(NeedOverviewRequested)
            }
            leftButton?.apply {
                text = "Abort Mission"
                setOnClickListener{
                    fAdapter.selection?.mission?.abort()
                }
            }
        }
    }

}
