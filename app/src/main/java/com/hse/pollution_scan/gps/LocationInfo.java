package com.hse.pollution_scan.gps;

import java.util.Date;

public class LocationInfo {

    private double _latitude;
    private double _longitude;
    private long _time;
    private String _value;

    public LocationInfo(long time, double latitude, double longitude, String value){
        _time = time;
        _latitude = latitude;
        _longitude = longitude;
        _value = value;
    }

    public double getLatitude(){
        return _latitude;
    }

    public double getLongitude(){
        return _longitude;
    }

    public long getTime(){
        return _time;
    }

    public String getValue() {
        return _value;
    }

    public String getTimeText(){
        Date time = new Date(_time);
        return time.toString();
    }

    public Date getFormatTime(){
        return new Date(_time);
    }

    public static LocationInfo getLocationInfoFromString(String stringForParsing){
        String[] parts = stringForParsing.split(",");

        long time = Long.parseLong(parts[0]);
        double latitude = Double.parseDouble(parts[1]);
        double longitude = Double.parseDouble(parts[2]);
        String value = parts[3];

        return new LocationInfo(time,latitude,longitude, value);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(_time);
        sb.append(",");
        sb.append(_latitude);
        sb.append(",");
        sb.append(_longitude);
        sb.append(",");
        sb.append(_value);

        return sb.toString();
    }
}
