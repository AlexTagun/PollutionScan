package com.hse.pollution_scan;

import com.hse.pollution_scan.gps.LocationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class PollutionInfo{
    public double pollution;
    public long time;

    public PollutionInfo(double Pollution, long Time) {
        pollution = Pollution;
        time = Time;
    }
}

public class PollutionAnalyzer {
    private static final int  MIN_TIME_BETWEEN_LOCATIONS_IN_SECONDS= 60;

    public static int CorrectPointsCount = -1;

    public static PollutionInfo calculate(List<LocationInfo> locationInfos){
        locationInfos = removeDuplicate(locationInfos);
        CorrectPointsCount = locationInfos.size();

        PollutionInfo pollutionInfo = calculateAverage(locationInfos);

        return pollutionInfo;
    }

    private static List<LocationInfo> removeDuplicate(List<LocationInfo> locationInfos){
        List<LocationInfo> toRemove = new ArrayList<>();

        //first locationInfo for starting date
        if(locationInfos.size() < 3){
            return locationInfos;
        }

        for(int i = 2; i < locationInfos.size(); i++){
            int prevIndex = i - 1;
            LocationInfo prevLocation = locationInfos.get(prevIndex);
            long prevTime = prevLocation.getTime();

            LocationInfo currentLocation = locationInfos.get(i);
            long currentTime = currentLocation.getTime();

            long diffTime = currentTime - prevTime;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffTime);
            if(seconds < MIN_TIME_BETWEEN_LOCATIONS_IN_SECONDS){
                toRemove.add(prevLocation);
            }
        }

        for (int i = 0; i < toRemove.size(); i++){
            locationInfos.remove(toRemove.get(i));

        }

        return locationInfos;
    }

    private static PollutionInfo calculateAverage(List<LocationInfo> locationInfos){
        long startTime = locationInfos.get(0).getTime();
        long endTime = locationInfos.get(locationInfos.size() - 1).getTime();
        long duration = endTime - startTime;

        List<PollutionInfo> pollutionInfos = getPollutionByLocations(locationInfos);

        if(pollutionInfos.size() == 1){
            return new PollutionInfo(pollutionInfos.get(0).pollution, duration);
        }

        double pollutionValue = 0;

        for(int i = 1; i < pollutionInfos.size(); i++){
            int prevIndex = i - 1;
            PollutionInfo prevPollutionInfo = pollutionInfos.get(prevIndex);
            long prevTime = prevPollutionInfo.time;

            PollutionInfo currentPollutionInfo = pollutionInfos.get(i);
            long currentTime = currentPollutionInfo.time;

            double timeDiff = (double) (currentTime - prevTime);
            double proportia = timeDiff / ((double)duration);
            pollutionValue += proportia * currentPollutionInfo.pollution;
        }

        return new PollutionInfo(pollutionValue, duration);
    }

    private static List<PollutionInfo> getPollutionByLocations(List<LocationInfo> locationInfos){
        List<PollutionInfo> pollutionInfos = new ArrayList<>();

        for(int i = 0; i < locationInfos.size(); i++){
            pollutionInfos.add(getPollutionByLocation(locationInfos.get(i)));
        }

        return pollutionInfos;
    }

    private static PollutionInfo getPollutionByLocation(LocationInfo locationInfo){
        double maxDistance = Double.MAX_VALUE;
        double pollutionValue = -1;

        List<MapsActivity.Point> points = MapsActivity._points;

        for (int i = 0; i < points.size(); i++){
            MapsActivity.Point currentPoint = points.get(i);

            if(tryParseInt(currentPoint.value)){
                double currentDistance = currentPoint.calculateDistanceFromPoint(locationInfo.getLatitude(), locationInfo.getLongitude());

                if(currentDistance < maxDistance){
                    maxDistance = currentDistance;
                    pollutionValue = Integer.parseInt(currentPoint.value);
                }
            }
        }

        PollutionInfo pollutionInfo = new PollutionInfo(pollutionValue, locationInfo.getTime());

        return pollutionInfo;
    }

    private static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
