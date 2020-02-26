package com.hse.pollution_scan.maps;

import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hse.pollution_scan.MainActivity;

public class MapsPoint {
    private double x;
    private double y;
    public String value;
    private PointType pointType;

    public MapsPoint(double x, double y, String value, PointType pointType){
        this.x = x;
        this.y = y;
        this.value = value;
        this.pointType = pointType;

        TryAddToPoints();
    }

    public LatLng getPosition(){
        return new LatLng(x,y);
    }

    private void TryAddToPoints(){
        switch (pointType){
            case Player:
                break;
            case Station:
                MapsPoints._points.add(this);
                break;
            default:
                Log.e(MapsPoint.class.getSimpleName(), "Not found pointType with name = " + pointType);
                break;
        }
    }

    public void drawMarker(){

        LatLng marker = new LatLng(x,y);
        float descriptor;

        if(pointType == PointType.Player){
            descriptor = BitmapDescriptorFactory.HUE_CYAN;
        }
        else if (tryParseValue() >= 80){descriptor = BitmapDescriptorFactory.HUE_RED;}
        else if (tryParseValue() >= 50) {descriptor = BitmapDescriptorFactory.HUE_ORANGE;
        }
        else {descriptor = BitmapDescriptorFactory.HUE_GREEN;}

        MainActivity.mMap.addMarker(new MarkerOptions()
                .position(marker).icon(BitmapDescriptorFactory.defaultMarker(descriptor))
                .title(value));
    }

    private int tryParseValue(){
        if(value.equals("-")) return -1;
        return Integer.parseInt(value);
    }

    public double calculateDistanceFromPoint(double otherX, double otherY){
        return calculateDistanceBetweenPoints(x, y, otherX, otherY);
    }

    private double calculateDistanceBetweenPoints(
            double x1,
            double y1,
            double x2,
            double y2) {
        return Math.sqrt( Math.abs(y2 - y1) * Math.abs(y2 - y1) + Math.abs(x2 - x1) * (Math.abs(x2 - x1)));
    }
}
