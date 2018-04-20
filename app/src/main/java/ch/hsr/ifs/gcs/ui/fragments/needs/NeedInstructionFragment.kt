package ch.hsr.ifs.gcs.ui.fragments.needs

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NeedInstructionFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnNeedInstructionFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_need_instruction, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            listener = context.fragmentHandler!!.needInstructionListener
        } else {
            throw RuntimeException(context.toString() + " must implement OnNeedInstructionFragmentListener")
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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) = NeedInstructionFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }

    }

}
