package ch.hsr.ifs.gcs.model

class CallIn : Need {

    private val targetParameter = ChooseTargetNeedParameter()
    private val cargoParameter = ChooseCargoNeedParameter()

    override val name get() = "Call-in"

    override val needParameterList: List<NeedParameter<Any>> = arrayListOf(
            targetParameter as NeedParameter<Any>,
            cargoParameter as NeedParameter<Any>
    )

    override var isActive = false

    override fun getTasks(): List<Task>? {
        if(targetParameter.isCompleted && cargoParameter.isCompleted) {
            val targetLocation = targetParameter.result
            val cargo = cargoParameter.result
            return ArrayList()
            // TODO: Generate list of tasks
        } else {
            return null
        }
    }

}