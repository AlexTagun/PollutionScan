package com.hse.pollution_scan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;

import com.hse.pollution_scan.gps.LocationInfo;
import com.hse.pollution_scan.maps.JsonPointParser;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.core.app.TaskStackBuilder;

/**
 * Class to process location results.
 */
public class LocationResultHelper {

    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";

    final static String KEY_LOCATION_UPDATES_COUNT = "location-update-count";

    final static int MAX_LOCATION_COUNT = 10000;

    final private static String PRIMARY_CHANNEL = "default";


    private Context mContext;
    private List<Location> mLocations;
    private NotificationManager mNotificationManager;

    LocationResultHelper(Context context, List<Location> locations) {
        mContext = context;
        mLocations = locations;

        NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL,
                context.getString(R.string.default_channel), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getNotificationManager().createNotificationChannel(channel);
    }

    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     */
    private String getLocationResultTitle() {
        int locationCount = getLocationCount(mContext);

        String numLocationsReported;

        if(locationCount < MAX_LOCATION_COUNT){
            numLocationsReported = mContext.getResources().getQuantityString(
                    R.plurals.num_locations_reported, locationCount, locationCount);
        }else{
            numLocationsReported = mContext.getResources().getString(R.string.max_locations_reported);
        }

        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    private String getLocationResultText() {
        if (mLocations.isEmpty()) {
            return mContext.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            sb.append(new Date(location.getTime()));
            sb.append("\n");
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    static int getLocationCount(Context context){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        boolean isContainsLocationCount = PreferenceManager.getDefaultSharedPreferences(context).contains(KEY_LOCATION_UPDATES_COUNT);

        if(!isContainsLocationCount){
            editor.putString(KEY_LOCATION_UPDATES_COUNT, Integer.toString(0));
            editor.apply();
        }

        int locationCount = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LOCATION_UPDATES_COUNT,"0"));

        return locationCount;
    }

    /**
     * Saves location result as a string to {@link android.content.SharedPreferences}.
     */
    void saveResults() {
//        PreferenceManager.getDefaultSharedPreferences(mContext)
//                .edit()
//                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle() + "\n" +
//                        getLocationResultText())
//                .apply();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        List<String> locationStrings = getLocationStringsForSaving();

        int locationCount = getLocationCount(mContext);

        for(int i = 0; i < locationStrings.size(); i++){
            if(locationCount < MAX_LOCATION_COUNT){
                editor.putString(KEY_LOCATION_UPDATES_RESULT + locationCount, locationStrings.get(i));

                locationCount++;
            }
        }

        editor.putString(KEY_LOCATION_UPDATES_COUNT, Integer.toString(locationCount));

        editor.apply();
    }

    List<String> getLocationStringsForSaving(){
        List<String> LocationStrings = new ArrayList<>();


        if (mLocations.isEmpty()) {
            return LocationStrings;
        }

        StringBuilder sb = new StringBuilder();
        for (Location location : mLocations) {
            sb.append(location.getTime());
            sb.append(",");
            sb.append(location.getLatitude());
            sb.append(",");
            sb.append(location.getLongitude());

            String dataForGettingValue = location.getLatitude() + "," + location.getLongitude();
            String valueString = JsonPointParser.getValueFromServer(dataForGettingValue);

            sb.append(",");
            sb.append(valueString);

            LocationStrings.add(sb.toString());

            sb.setLength(0);
        }

        return LocationStrings;
    }

    public static List<LocationInfo> getLocationInfos(Context context){
        List<LocationInfo> locationInfos = new ArrayList<>();

        int locationCount = getLocationCount(context);

        for(int i = 0; i < locationCount; i++){
            String locationString = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LOCATION_UPDATES_RESULT + i, "");
            LocationInfo locationInfo = LocationInfo.getLocationInfoFromString(locationString);
            locationInfos.add(locationInfo);
        }

        return locationInfos;
    }

    /**
     * Fetches location results from {@link android.content.SharedPreferences}.
     */
    static String getSavedLocationResult(Context context) {



        int locationCount = getLocationCount(context);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < locationCount; i++){
            String currentLocation = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_LOCATION_UPDATES_RESULT + i, "");
            sb.append(currentLocation);
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    static void clearLocationSaves(Context context){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        int locationCount = getLocationCount(context);

        for(int i = 0; i < locationCount; i++){
            editor.remove(KEY_LOCATION_UPDATES_RESULT + i);
        }

        editor.putString(KEY_LOCATION_UPDATES_COUNT, Integer.toString(0));

        editor.apply();
    }

    /**
     * Get the notification mNotificationManager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    /**
     * Displays a notification with the location results.
     */
    void showNotification() {
        Intent notificationIntent = new Intent(mContext, MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(mContext,
                PRIMARY_CHANNEL)
                .setContentTitle(getLocationResultTitle())
                .setContentText(getLocationResultText())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(notificationPendingIntent);

        getNotificationManager().notify(0, notificationBuilder.build());
    }
}
