package com.hse.pollution_scan.maps;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.hse.pollution_scan.MapsActivity.mMap;

public class MapsPoint {
    private double x;
    private double y;
    public String value;

    public MapsPoint(double x, double y, String value){
        this.x = x;
        this.y = y;
        this.value = value;

        MapsPoints._points.add(this);
    }

    public LatLng getPosition(){
        return new LatLng(x,y);
    }

    public void drawMarker(){
        LatLng marker = new LatLng(x,y);
        float descriptor;
        if(tryParseValue() >= 50) descriptor = BitmapDescriptorFactory.HUE_ORANGE;
        else descriptor = BitmapDescriptorFactory.HUE_GREEN;

        mMap.addMarker(new MarkerOptions()
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
