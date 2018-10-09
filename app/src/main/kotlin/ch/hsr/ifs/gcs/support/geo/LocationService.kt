package ch.hsr.ifs.gcs.support.geo

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

class LocationService(val context: Context, private val listener: OnLocationChangedListener?) : LocationListener {

    companion object {
        private val LOG_TAG = LocationService::class.java.simpleName
    }

    private var currentLocation: Location? = null

    private val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager?

    init {
        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
        } catch(e: SecurityException) {
            Log.e(LOG_TAG, e.message)
        }
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        listener?.onCurrentLocationChanged(location)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    interface OnLocationChangedListener {
        fun onCurrentLocationChanged(location: Location)
    }

}