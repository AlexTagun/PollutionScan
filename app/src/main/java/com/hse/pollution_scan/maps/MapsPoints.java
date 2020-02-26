package com.hse.pollution_scan.maps;

import android.content.Context;

import com.hse.pollution_scan.LocationResultHelper;
import com.hse.pollution_scan.MainActivity;
import com.hse.pollution_scan.gps.LocationInfo;

import java.util.ArrayList;
import java.util.List;

public class MapsPoints{
    public static List<MapsPoint> _points = new ArrayList<>();

    private static void drawPlayerPosition(){
        Context mContext = MainActivity.GetContext();
        List<LocationInfo> locationInfos = LocationResultHelper.getLocationInfos(mContext);

        int lastIndex = locationInfos.size() - 1;

        if(lastIndex > 0){
            LocationInfo lastLocation = locationInfos.get(lastIndex);

            MapsPoint point = new MapsPoint(lastLocation.getLatitude(),lastLocation.getLongitude(), "100", PointType.Player);
            point.drawMarker();
        }
    }

    public static void drawPoints(){
        //drawPlayerPosition();
        for (MapsPoint point: _points) point.drawMarker();
    }
}




