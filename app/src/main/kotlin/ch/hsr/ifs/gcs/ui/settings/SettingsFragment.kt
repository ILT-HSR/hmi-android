package ch.hsr.ifs.gcs.ui.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import ch.hsr.ifs.gcs.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

}