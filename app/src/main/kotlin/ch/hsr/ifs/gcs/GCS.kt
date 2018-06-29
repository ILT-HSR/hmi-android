package ch.hsr.ifs.gcs

import android.app.Application
import android.util.Log

class GCS : Application() {

    companion object {
        private val LOG_TAG = GCS::class.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(LOG_TAG, "Application created")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.i(LOG_TAG, "Application terminated")
    }

}