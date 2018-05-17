package ch.hsr.ifs.gcs.ui.fragments.needs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.model.Need
import ch.hsr.ifs.gcs.model.NeedParameter
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.*
import kotlinx.android.synthetic.main.fragment_need_instruction_list.view.*

class NeedInstructionFragment : Fragment() {

    var activeNeed: Need? = null
    var activeNeedParameterList: List<NeedParameter<*>>? = null
    private var currentTaskId = 0

    private val TAG = NeedInstructionFragment::class.java.simpleName

    private var listener: OnNeedInstructionFragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            listener = context.fragmentHandler!!.needInstructionListener
        } else {
            throw RuntimeException(context.toString() + " must implement OnNeedInstructionFragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_need_instruction_list, container, false)
        val list = view.instructionList
        if (list is RecyclerView) {
            with(list) {
                layoutManager = LinearLayoutManager(context)
                adapter = NeedInstructionRecyclerViewAdapter(activeNeedParameterList!!)
            }
        }
        view.titleText.text = "New ${activeNeed!!.name}"
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context
        if(context is MainActivity) {
            activity.leftButton.setOnClickListener {
                context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
                activity.leftButton.background = context.applicationContext.getDrawable(R.drawable.cancel_action)
            }
            activeNeedParameterList?.get(currentTaskId)?.let {
                it.setup(context)
                it.isActive = true
            }
            needNavigationButton.setOnClickListener {
                activeNeedParameterList?.get(currentTaskId)?.let {
                    it.isActive = false
                    it.isCompleted = true
                    it.cleanup(context)
                }
                if(currentTaskId < activeNeedParameterList!!.size - 1) {
                    currentTaskId += 1
                    activeNeedParameterList?.get(currentTaskId)?.let {
                        it.setup(context)
                        it.isActive = true
                    }
                } else {
                    needNavigationButton.text = "Start Mission"
                    needNavigationButton.setBackgroundColor(Color.parseColor("#68e180"))
                    TODO("Resources not available") // Start mission with gathered results
                }
                view!!.instructionList.adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Defines functions to be overwritten by the context making use of this fragment.
     */
    interface OnNeedInstructionFragmentListener {

        /**
         * TODO: Implement behaviour of fragment
         */
        fun onNeedInstructionFragmentChanged()
    }

}
