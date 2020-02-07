package com.hse.pollution_scan.maps;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hse.pollution_scan.LocationResultHelper;
import com.hse.pollution_scan.MainActivity;
import com.hse.pollution_scan.gps.LocationInfo;

import java.util.ArrayList;
import java.util.List;

import static com.hse.pollution_scan.MapsActivity.mMap;

public class MapsPoints{
    public static List<MapsPoint> _points = new ArrayList<>();

    private void drawPlayerPosition(){
        Context mContext = MainActivity.GetContext();
        List<LocationInfo> locationInfos = LocationResultHelper.getLocationInfos(mContext);

        for(LocationInfo location : locationInfos){
            MapsPoint point = new MapsPoint(location.getLatitude(),location.getLongitude(), "100");
            point.drawMarker();
        }
    }

    public static void drawPoints(){

        for (MapsPoint point: _points) point.drawMarker();
    }

}




