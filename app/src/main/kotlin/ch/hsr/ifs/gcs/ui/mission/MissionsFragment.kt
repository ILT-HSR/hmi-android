package ch.hsr.ifs.gcs.ui.mission

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.GCS
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.MainModel
import ch.hsr.ifs.gcs.NeedOverviewRequested
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

        activity?.apply {
            statusesAddButton.setOnClickListener {
                fModel.submit(NeedOverviewRequested)
            }
            leftButton?.apply {
                background = applicationContext?.getDrawable(R.drawable.cancel_action)
                setOnClickListener{
                    fAdapter.selection?.mission?.abort()
                }
            }
        }
    }

}
