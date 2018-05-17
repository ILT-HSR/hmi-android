package ch.hsr.ifs.gcs.model

class CallIn : Need {

    override val name get() = "Call-in"

    override val needParameterList: List<NeedParameter<Any>> = arrayListOf(ChooseTargetNeedParameter() as NeedParameter<Any>, ChooseCargoNeedParameter() as NeedParameter<Any>)

    override var isActive = false

    override fun translateToFunctionList(): List<Task> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}