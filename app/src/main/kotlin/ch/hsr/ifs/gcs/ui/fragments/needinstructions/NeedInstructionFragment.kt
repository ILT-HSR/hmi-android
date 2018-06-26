package ch.hsr.ifs.gcs.ui.fragments.needinstructions

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.Mission
import ch.hsr.ifs.gcs.mission.access.MissionProvider
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.mission.need.NeedItem
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.view.*

class NeedInstructionFragment : Fragment() {


    /**
     * Defines functions to be overwritten by the context making use of this fragment.
     */
    interface OnNeedInstructionFragmentListener {

        fun onNeedInstructionFragmentChanged()

    }

    private var fCurrentParaneterId = 0
    private var fListener: OnNeedInstructionFragmentListener? = null

    private lateinit var fNeed: NeedItem
    private lateinit var fParameters: List<ParameterItem<*>>
    private lateinit var fAdapter: NeedInstructionRecyclerViewAdapter

    var need
        get() = fNeed
        set(value) {
            fNeed = value
            fParameters = fNeed.parameters
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            fListener = context.fragmentHandler!!.needInstructionListener
        } else {
            throw IllegalArgumentException(context.toString() + " must implement OnNeedInstructionFragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_need_instruction_list, container, false)
        val list = view.instructionList
        if (list is RecyclerView) {
            with(list) {
                fAdapter = NeedInstructionRecyclerViewAdapter(need, context as MainActivity)
                layoutManager = LinearLayoutManager(context)
                adapter = fAdapter
            }
        }
        view.titleText.text = "New ${need.name}"
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context
        if (context is MainActivity) {
            activity?.apply {
                setupCancelButton(context)
                selectFirstParameter(context)
                needNavigationButton.setOnClickListener {
                    finishCurrentParameter(context)
                    if (fCurrentParaneterId < fParameters.size - 1) {
                        selectNextParameter(context)
                    } else {
                        finishNeedSetup(context)
                    }
                    fAdapter.notifyDataSetChanged()
                }

            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        fListener = null
    }

    private fun setupCancelButton(context: MainActivity) {
        context.leftButton.setOnClickListener {
            context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
            context.leftButton.background = context.applicationContext.getDrawable(R.drawable.cancel_action)
        }
    }

    private fun selectFirstParameter(context: MainActivity) {
        fCurrentParaneterId = 0
        fParameters[fCurrentParaneterId].apply {
            activate()
            setup(context)
        }
    }

    private fun finishCurrentParameter(context: MainActivity) {
        fParameters[fCurrentParaneterId].apply {
            deactivate()
            markComplete()
            cleanup(context)
        }
    }

    private fun selectNextParameter(context: MainActivity) {
        fCurrentParaneterId += 1
        fParameters[fCurrentParaneterId].apply {
            setup(context)
            activate()
        }
    }

    private fun FragmentActivity.finishNeedSetup(context: MainActivity) {
        needNavigationButton.text = getString(R.string.button_start_mission)
        needNavigationButton.setBackgroundColor(Color.parseColor("#68e180"))
        need.let { Mission(it.need) }.let(MissionProvider::submit)
        needNavigationButton.setOnClickListener {
            context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.MISSION_STATUSES_FRAGMENT)
            context.leftButton?.background = context.applicationContext.getDrawable(R.drawable.abort_mission)
        }
    }

}
