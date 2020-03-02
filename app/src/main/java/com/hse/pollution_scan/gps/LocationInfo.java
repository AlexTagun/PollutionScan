package com.hse.pollution_scan.gps;

import java.util.Date;

public class LocationInfo {

    private double _latitude;
    private double _longitude;
    private long _time;

    public LocationInfo(long time, double latitude, double longitude){
        _time = time;
        _latitude = latitude;
        _longitude = longitude;
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

        return new LocationInfo(time,latitude,longitude);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(_time);
        sb.append(",");
        sb.append(_latitude);
        sb.append(",");
        sb.append(_longitude);

        return sb.toString();
    }
}
