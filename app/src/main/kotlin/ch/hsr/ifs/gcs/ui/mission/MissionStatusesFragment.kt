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
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.MainModel
import ch.hsr.ifs.gcs.NeedOverviewRequested
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.*
import kotlinx.android.synthetic.main.fragment_missionstatuses_list.view.*


/**
 * A fragment representing a list of mission status items combined with a button to add
 * additional needs. The context containing this fragment must implement the
 * [MissionStatusesFragment.OnStatusesFragmentChangedListener] interface.
 */
class MissionStatusesFragment : Fragment(), Input.Listener {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: MissionStatusesRecyclerViewAdapter

    private var fControls: Input? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_missionstatuses_list, container, false)
        val list = view.list
        if (list is RecyclerView) {
            with(list) {
                fAdapter = MissionStatusesRecyclerViewAdapter(this)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fModel = (activity!!.application as GCS).mainModel
        fModel.activeMissions.observe(this, Observer {
            fAdapter.missions = it ?: emptyList()
        })
        fModel.availableNeeds.observe(this, Observer {
            statusesAddButton.isEnabled = it != null && it.isNotEmpty()
        })
        fModel.activeInputDevice.observe(this, Observer {
            fControls = it
        })

        fControls = fModel.activeInputDevice.value
        fControls?.addListener(this)

        activity?.apply {
            statusesAddButton.setOnClickListener {
                fModel.submit(NeedOverviewRequested())
            }
            leftButton?.apply {
                background = applicationContext?.getDrawable(R.drawable.cancel_action)
                setOnClickListener{
                    fAdapter.selection?.abort()
                }
            }
        }
    }

    // Input.Listener implementation

    override fun onButton(control: Input.Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Input.Control.DPAD_UP -> {
                fAdapter.activatePreviousItem()
            }
            Input.Control.DPAD_DOWN -> {
                fAdapter.activateNextItem()
            }
            Input.Control.DPAD_RIGHT -> {
                // TODO: Implement MissionResults switch
                // fModel.submit(ResultsOverviewRequested())
                fControls?.removeListener(this)
            }
            Input.Control.NEED_START -> {
                fModel.submit(NeedOverviewRequested())
                fControls?.removeListener(this)
            }
        }
    }
}
