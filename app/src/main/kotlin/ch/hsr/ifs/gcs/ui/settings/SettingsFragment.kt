package ch.hsr.ifs.gcs.ui.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import ch.hsr.ifs.gcs.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }

}