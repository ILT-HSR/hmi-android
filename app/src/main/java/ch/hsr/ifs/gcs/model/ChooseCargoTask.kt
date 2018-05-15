package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.ChooseCargoFragment

class ChooseCargoTask : Task<String> {

    override val name get() = "Cargo"

    override val description get() = "Select the cargo involved in your mission."

    override var result: String? = ""

    override fun resultToString(): String {
        return result!!
    }

    override var isActive = false

    override var isCompleted = false

    val fragment = ChooseCargoFragment()

    override fun setup(context: MainActivity) {
        fragment.task = this
        val transaction = context.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mapholder, fragment)
        transaction.commit()
    }

    override fun cleanup(context: MainActivity) {
        val transaction = context.supportFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commit()
    }

}