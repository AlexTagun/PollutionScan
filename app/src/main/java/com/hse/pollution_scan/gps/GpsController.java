package com.hse.pollution_scan.gps;

import android.content.Context;
import android.location.LocationManager;

public class GpsController {

    private GpsUtils _gpsUtils;
    private GpsUtils.onGpsListener _onGpsListener;

    public GpsController(Context context){
        _gpsUtils = new GpsUtils(context);

        _onGpsListener = new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
            }
        };
    }

    public void turnGPSOn(){
        _gpsUtils.turnGPSOn(_onGpsListener);
    }

    public boolean isGPS(){
        return _gpsUtils.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
