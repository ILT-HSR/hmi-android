package ch.hsr.ifs.gcs.ui.mission.need

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.*
import ch.hsr.ifs.gcs.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_list.*
import kotlinx.android.synthetic.main.fragment_need_list.view.*

/**
 * A fragment representing a list of Items.
 */
class NeedsFragment : Fragment() {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: NeedsRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                fAdapter = NeedsRecyclerViewAdapter(list, context as MainActivity)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fModel = (activity!!.application as GCS).mainModel
        fModel.availableNeeds.observe(this, Observer {
            fAdapter.needs = it ?: emptyList()
        })

        activity?.apply {
            selectButton.setOnClickListener {
                fModel.submit(NeedConfigurationStarted(fAdapter.activeItem.need))
            }

            // TODO: Move to model/activity?
            leftButton.text = "Cancel"
            leftButton.setOnClickListener {
                fModel.submit(MissionOverviewRequested)
            }
        }
    }

}
