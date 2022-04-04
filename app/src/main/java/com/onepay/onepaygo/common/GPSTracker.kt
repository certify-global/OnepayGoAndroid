package com.onepay.onepaygo.common

import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.content.Intent
import android.os.IBinder
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.location.Location
import com.onepay.onepaygo.data.AppSharedPreferences
import java.lang.Exception

class GPSTracker(private val mContext: Context) : Service(), LocationListener {
    var locationObj: Location? = null
        private set
    private var latitude = 0.0
    private var longitude = 0.0
    private var provider_info = ""
    var isGPSEnabled = false
    var isNetworkEnabled = false
    @SuppressLint("MissingPermission")
    fun getLocation() {
        try {
            if (!Utils.PermissionCheck(mContext, Utils.location)) return
            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager
            if (locationManager != null) {
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                isNetworkEnabled =
                    locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
            if (isGPSEnabled) {
                provider_info = LocationManager.GPS_PROVIDER
                AppSharedPreferences.writeSp(AppSharedPreferences.getSharedPreferences(mContext), PreferencesKeys.locationStatus, true)
            } else if (isNetworkEnabled) {
                provider_info = LocationManager.NETWORK_PROVIDER
                AppSharedPreferences.writeSp(AppSharedPreferences.getSharedPreferences(mContext), PreferencesKeys.locationStatus, true)
            } else {
                AppSharedPreferences.writeSp(AppSharedPreferences.getSharedPreferences(mContext), PreferencesKeys.locationStatus, false)
            }
            if (!provider_info.isEmpty()) {
                locationManager!!.requestLocationUpdates(
                    provider_info,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                    this
                )
                locationObj = locationManager!!.getLastKnownLocation(provider_info)
                updateGPSCoordinates()
            }
        } catch (e: Exception) {
            Logger.error(LOG + "getLocation()", e.message)
        }
    }

    private fun updateGPSCoordinates() {
        try {
            if (locationObj != null) {
                latitude = locationObj!!.latitude
                longitude = locationObj!!.longitude
            }
        } catch (e: Exception) {
            Logger.error(LOG + "updateGPSCoordinates()", e.message)
        }
    }

    fun getLatitude(): Double {
        try {
            if (locationObj != null) {
                latitude = locationObj!!.latitude
            }
        } catch (e: Exception) {
            Logger.error(LOG + "getLatitude()", e.message)
        }
        return latitude
    }

    fun getLongitude(): Double {
        try {
            if (null != locationObj) {
                longitude = locationObj!!.longitude
            }
        } catch (e: Exception) {
            Logger.error(LOG + "getLongitude()", e.message)
        }
        return longitude
    }

    override fun onLocationChanged(location: Location) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        thisObj = null
        super.onDestroy()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var thisObj: GPSTracker? = null
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
        private const val MIN_TIME_BW_UPDATES = (1000 * 60).toLong()
        private const val LOG = "GPSTracker - "
        private var locationManager: LocationManager? = null
        fun stopGPS() {
            try {
                if (locationManager != null && thisObj != null) locationManager!!.removeUpdates(
                    thisObj!!
                )
                locationManager = null
            } catch (e: Exception) {
                Logger.error("stopGPS()", e.message)
            }
        }
    }

    init {
        getLocation()
        thisObj = this
    }
}