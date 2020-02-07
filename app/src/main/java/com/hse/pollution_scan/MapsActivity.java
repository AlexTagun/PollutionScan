package com.hse.pollution_scan;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.hse.pollution_scan.maps.JsonPointParser;
import com.hse.pollution_scan.maps.MapsPoint;
import com.hse.pollution_scan.maps.MapsPoints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

    public void toMain(View view){
        finish();
    }
}
