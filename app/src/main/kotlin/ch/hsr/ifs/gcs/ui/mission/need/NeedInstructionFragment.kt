package ch.hsr.ifs.gcs.ui.mission.need

import android.graphics.Color
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
import kotlinx.android.synthetic.main.fragment_need_instruction_list.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.view.*

class NeedInstructionFragment : Fragment() {

    private lateinit var fModel: MainModel
    private lateinit var fAdapter: NeedInstructionRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_instruction_list, container, false)
        val list = view.instructionList
        if (list is RecyclerView) {
            with(list) {
                fAdapter = NeedInstructionRecyclerViewAdapter(context as MainActivity)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = activity as MainActivity
        fModel = (activity!!.application as GCS).mainModel

        fAdapter.parameters = fModel.activeNeed.value!!.parameterList
        fModel.activeNeed.value?.let { need ->
            val item = context.needItemFactory.instantiate(need)
            view!!.titleText.text = "New ${item.name}"
            needNavigationButton.setOnClickListener{ _ ->
                fAdapter.completeCurrent()
                if(fAdapter.isDone) {
                    needNavigationButton.text = getString(R.string.button_start_mission)
                    needNavigationButton.setBackgroundColor(Color.parseColor("#68e180"))
                    needNavigationButton.setOnClickListener {
                        fModel.submit(NeedConfigurationFinished)
                    }
                }
            }
        }

        activity?.apply {
            leftButton?.setOnClickListener {
                fAdapter.abort()
                fModel.submit(NeedOverviewRequested)
            }
        }
    }

}
