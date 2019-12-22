package com.hse.pollution_scan;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Point> _points = new ArrayList<>();

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
//        LatLng marker = new LatLng(55.80860455111258,38.90439907852537);
//        mMap.addMarker(new MarkerOptions()
//                .position(marker)
//                .title("Ты пидор"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 12));





        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        JSONArray data = getDataFromURL();
        if(data == null) return;
        for(int i = 0; i < data.length(); i++){
            try {
                JSONObject point = data.getJSONObject(i);
                parseData(point);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Point point: _points) point.drawMarker();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(_points.get(0).getPosition(), 9));
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private JSONArray getDataFromURL() {
        try {
            URL url = null;
            url = new URL("http://aqicn.org/map/moscow/ru/#");
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line = "";
            ArrayList<String> myTextView = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                myTextView.add(line);
            }
            int a = myTextView.toString().indexOf("mapInitWithData");
            String substring = myTextView.toString().substring(a + 16);
            int b = substring.indexOf(";");
            substring = substring.substring(0, b - 1);
            int c = substring.lastIndexOf("meta");
            substring = substring.substring(0, c - 3);

            try {
                return new JSONArray(substring);
            } catch (Throwable t) {
                Log.e("PAGE", "Could not parse malformed JSON: \"" + substring + "\"");
            }

        } catch (Exception e) {

            Log.e("ERR", Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    private class Point{
        private double x;
        private double y;
        private String value;

        Point(double x, double y, String value){
            this.x = x;
            this.y = y;
            this.value = value;

            _points.add(this);
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
    }

    private void parseData(JSONObject point){
        String coordinates;
        String valueS = null;
        try {
            coordinates = point.getString("g");
            valueS = point.getString("aqi");
            Log.i("PAGE", valueS);

            String xs = coordinates.substring(1, 9);
            String ys = coordinates.substring(11, 19);
            double x = Double.parseDouble(xs);
            double y = Double.parseDouble(ys);


//            Log.i("PAGE", Float.toString(x));
//            Log.i("PAGE", Float.toString(y));

            new Point(x, y, valueS);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
