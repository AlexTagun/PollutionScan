package com.hse.pollution_scan.maps;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class JsonPointParser {

    public static String getValueFromServer(String data)
    {
        try {
            byte[] postData       = data.getBytes( StandardCharsets.UTF_8 );
            int    postDataLength = postData.length;
            String request        = "http://185.17.120.159:8081/";
            URL    url            = new URL( request );
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            conn.setDoOutput( true );
            conn.setInstanceFollowRedirects( false );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            conn.setUseCaches( false );
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
            }
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line = "";
            ArrayList<String> myTextView = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                myTextView.add(line);
            }

            String valueString = myTextView.get(0);

            Log.i(JsonPointParser.class.getSimpleName(), valueString);
            conn.connect();

            return valueString;
        } catch (Exception e) {
            Log.i(JsonPointParser.class.getSimpleName(), "EROOR");
            //Log.e("ERR", Objects.requireNonNull(e.getMessage()));

            return "-";
        }
    }

    public static JSONArray getDataFromURL() {
        try {
            URL url = null;
            url = new URL("https://aqicn.org/map/moscow/ru/#");
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

    public static void parseData(JSONObject point){
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

            new MapsPoint(x, y, valueS, PointType.Station);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
