package ch.hsr.ifs.gcs.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.hsr.ifs.gcs.ui.settings.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

}