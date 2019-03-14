package ch.hsr.ifs.gcs.ui

import android.app.Activity
import android.os.Bundle
import ch.hsr.ifs.gcs.ui.settings.SettingsFragment

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

}