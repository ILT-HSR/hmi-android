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
import ch.hsr.ifs.gcs.driver.Input
import ch.hsr.ifs.gcs.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_list.*
import kotlinx.android.synthetic.main.fragment_need_list.view.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [NeedsFragment.OnNeedsFragmentChangedListener] interface.
 */
class NeedsFragment : Fragment(), Input.Listener {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: NeedsRecyclerViewAdapter

    private var fControls: Input? = null

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
        fModel.activeInputDevice.observe(this, Observer {
            fControls = it
        })

        fControls = fModel.activeInputDevice.value
        fControls?.addListener(this)

        activity?.apply {
            selectButton.setOnClickListener {
                fModel.submit(NeedConfigurationStarted(fAdapter.activeItem.need))
            }

            // TODO: Move to model/activity?
            leftButton?.background = applicationContext.getDrawable(R.drawable.cancel_action)
            leftButton.setOnClickListener {
                fModel.submit(MissionOverviewRequested())
            }
        }
    }

    override fun onButton(control: Input.Control) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (control) {
            Input.Control.DPAD_UP -> {
                fAdapter.activatePreviousItem()
            }
            Input.Control.DPAD_DOWN -> {
                fAdapter.activateNextItem()
            }
            Input.Control.UPDATE_ABORT -> {
                fModel.submit(MissionOverviewRequested())
                fControls?.removeListener(this)
            }
            Input.Control.NEED_START -> {
                selectButton?.performClick()
                fControls?.removeListener(this)
            }
        }
    }

}
