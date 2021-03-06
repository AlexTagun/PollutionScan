package com.hse.pollution_scan;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hse.pollution_scan.gps.GpsController;
import com.hse.pollution_scan.maps.JsonPointParser;
import com.hse.pollution_scan.maps.MapsPoint;
import com.hse.pollution_scan.maps.MapsPoints;
import com.yandex.metrica.YandexMetrica;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


/**
 * The only activity in this sample. Displays UI widgets for requesting and removing location
 * updates, and for the batched location updates that are reported.
 *
 * Location updates requested through this activity continue even when the activity is not in the
 * foreground. Note: apps running on "O" devices (regardless of targetSdkVersion) may receive
 * updates less frequently than the interval specified in the {@link LocationRequest} when the app
 * is no longer in the foreground.
 */
public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        OnMapReadyCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String NO_INTERNET_TEXT = "Нет доступа к интернету";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    // FIXME: 5/16/17
    private static final long UPDATE_INTERVAL = 10 * 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    // FIXME: 5/14/17
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

//    Location mLastLocation;
//    Marker mCurrLocationMarker;

    private static Context mContext;

    // UI Widgets.
    private Button mRequestUpdatesButton;
    private Button mRemoveUpdatesButton;
    private Button mCalculateResultButton;
    private TextView mLocationUpdatesResultView;

    private GpsController _gpsController;

    public static GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        mRequestUpdatesButton = (Button) findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (Button) findViewById(R.id.remove_updates_button);
        mCalculateResultButton = (Button) findViewById(R.id.calculate_result_button);
        mLocationUpdatesResultView = (TextView) findViewById(R.id.location_updates_result);

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleApiClient();

        initGPS();
        _gpsController.turnGPSOn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateButtonsState(LocationRequestHelper.getRequesting(this, _gpsController.isGPS()));
        //mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        JSONArray data = JsonPointParser.getDataFromURL();
        if(data == null) return;
        for(int i = 0; i < data.length(); i++){
            try {
                JSONObject point = data.getJSONObject(i);
                JsonPointParser.parseData(point);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        List<MapsPoint> points = MapsPoints._points;
        MapsPoints.drawPoints();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0).getPosition(), 9));

        mMap.setMyLocationEnabled(true);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
//        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {
        final String text = "Connection suspended";
        Log.w(TAG, text + ": Error code: " + i);
        showSnackbar("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        final String text = "Exception while connecting to Google Play services";
        Log.w(TAG, text + ": " + connectionResult.getErrorMessage());
        showSnackbar(text);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.activity_main);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted. Kick off the process of building and connecting
                // GoogleApiClient.
                buildGoogleApiClient();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
            //mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
        } else if (s.equals(LocationRequestHelper.KEY_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(LocationRequestHelper.getRequesting(this, _gpsController.isGPS()));
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates(View view) {
        if (!_gpsController.isGPS()) {
            _gpsController.turnGPSOn();
            Toast.makeText(this, "Пожалуйста, включите GPS", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            Log.i(TAG, "Starting location updates");
            LocationRequestHelper.setRequesting(this, true);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            LocationRequestHelper.setRequesting(this, false);
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        Log.i(TAG, "Removing location updates");
        LocationRequestHelper.setRequesting(this, false);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                getPendingIntent());
    }

    public void calculateResult(View view){
        if(MapsPoints._points.size() <= 0){
            Toast.makeText(this, "Скачивание точек на карту", Toast.LENGTH_SHORT).show();
            return;
        }

        if(LocationResultHelper.getLocationCount(this) <= 0){
            Toast.makeText(this, "Нет точек местоположения пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        PollutionInfo pollutionInfo = PollutionAnalyzer.calculate(LocationResultHelper.getLocationInfos(this));

        if(pollutionInfo.pollution <= 0){
            mLocationUpdatesResultView.setText(NO_INTERNET_TEXT);
            return;
        }

        String durationString = "";

        StringBuilder sb = new StringBuilder();

        sb.append("Количество корректных позиций: ");
        sb.append(PollutionAnalyzer.CorrectPointsCount);
        sb.append("\n");
        sb.append("\n");

        sb.append("Дата: ");
        sb.append("\n");

        Date duration = new Date(pollutionInfo.time);

        float hours = TimeUnit.MILLISECONDS.toHours(duration.getTime());
        if(0 < hours){
            sb.append("Часы = ");
            sb.append(hours);
            sb.append("\n");

            durationString = durationString + hours;
        }else{
            durationString = durationString + "0";
        }

        durationString = durationString + ":";

        float minutes = TimeUnit.MILLISECONDS.toMinutes(duration.getTime());
        if(0 < minutes){
            int formatMinutes = (int)(minutes % 60);

            sb.append("Минуты = ");
            sb.append(formatMinutes);
            sb.append("\n");

            durationString = durationString + formatMinutes;
        }else{
            durationString = durationString + "0";
        }

        durationString = durationString + ":";

        float seconds = TimeUnit.MILLISECONDS.toSeconds(duration.getTime());
        if(0 < seconds){
            int formatSeconds = (int)(seconds % 60);

            sb.append("Секунды = ");
            sb.append(formatSeconds);
            sb.append("\n");

            durationString = durationString + formatSeconds;
        }else{
            durationString = durationString + "0";
        }

        sb.append("Ваше загрязнение = ");
        sb.append(String.format("%.1f",pollutionInfo.pollution));

        String eventParameters = String.format("{\"value\":\"%.1f\", \"duration\":\"%s\"}", pollutionInfo.pollution, durationString);
        YandexMetrica.reportEvent("Pollution Info", eventParameters);

        mLocationUpdatesResultView.setText(sb.toString());

        clearLocations();
    }

    @Override
    public void onLocationChanged(Location location) {

//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }
//        //Place current location marker
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//
//        //move map camera
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//
//        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
//        }

    }

    public void clearLocations(){
        LocationResultHelper.clearLocationSaves(this);
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void updateButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestUpdatesButton.setEnabled(false);
            mRemoveUpdatesButton.setEnabled(true);
        } else {
            mRequestUpdatesButton.setEnabled(true);
            mRemoveUpdatesButton.setEnabled(false);
        }
    }

    private void initGPS(){
        if(_gpsController == null){
            _gpsController = new GpsController(this);
        }
    }

    public static Context GetContext(){
        return mContext;
    }

    private void showLongLog(String log){
        int maxLogSize = 1000;
        for(int i = 0; i <= log.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > log.length() ? log.length() : end;
            Log.v("PAGE", log.substring(start, end));
        }
    }
}