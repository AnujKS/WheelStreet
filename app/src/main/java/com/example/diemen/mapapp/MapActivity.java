package com.example.diemen.mapapp;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anujkumars on 10/19/2015.
 */

public class MapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = "MapActivity";
    public static final String EXTRA_INTERNET = "is_internet";

    GoogleApiClient mGoogleApiClient;
    public GoogleMap googleMap;
    public Marker myMarker;
    private Location myLocation;
    private boolean isAnimated = false;
    private static final int MIN_DISTANCE = 3;
    private static final float DEFAULT_ZOOM_LEVEL = 15.0f;
    BitmapDescriptor mMarkerIcon;
    BitmapDescriptor destinationMarkerIcon;
    LatLng origin;
    String Duration = "Fetching..";
    LatLng updateLoc;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Bundle extras = getIntent().getExtras();
        origin = new LatLng(Double.parseDouble(extras.getString("desLat")), Double.parseDouble(extras.getString("desLng")));

        buildGoogleApiClient();
        mMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
        destinationMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.destination);
        isConnected = getIntent().getBooleanExtra(EXTRA_INTERNET, false);
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                R.id.map_view));
        if (mapFragment != null) {
            googleMap = mapFragment.getMap();
            if (googleMap != null) {
                initMap();
            }
        }

    }

    //Direction Url
    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    //Download url
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //AsynTask
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJsonParser parser = new DirectionsJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) { // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(4);
                lineOptions.color(Color.RED);

            }
            Duration = duration;
            // tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);
            Toast.makeText(getApplicationContext(),
                    "Duration" + duration, Toast.LENGTH_SHORT)
                    .show();
            updateDuration();
            googleMap.addPolyline(lineOptions);
        }
    }

    //Update duration
    void updateDuration() {
        if (googleMap != null) {
            if (myMarker != null) {
                myMarker.remove();
            }
            MarkerOptions marker = new MarkerOptions()
                    .position(updateLoc)
                    .icon(mMarkerIcon);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(origin)
                    .icon(destinationMarkerIcon)
                    .title(Duration);


            googleMap.addMarker(markerOptions).showInfoWindow();
            myMarker = googleMap.addMarker(marker);

            if (!isAnimated) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(updateLoc, DEFAULT_ZOOM_LEVEL));
                isAnimated = true;
            }
        }
    }


    //-------------------------------------------------------------------------
    // map
    //-------------------------------------------------------------------------
    public void initMap() {
        // check if map is created successfully or not
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Unable to load map", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // Showing / hiding your current location
            googleMap.setMyLocationEnabled(true);
            // Enable / Disable my location button
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.setOnMyLocationChangeListener(myLocationChangeListener);

        }
    }

    private com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            if (myLocation == null || myLocation.distanceTo(location) > MIN_DISTANCE) {
                myLocation = location;

                if (origin != null) {
                    LatLng dest = new LatLng(location.getLatitude(), location.getLongitude());
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
                }


                Log.i(TAG, "onMyLocationChange Location:" + myLocation.getLatitude() + " : " + myLocation.getLongitude());
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                updateLoc = new LatLng(location.getLatitude(), location.getLongitude());

                if (googleMap != null) {
                    if (myMarker != null) {
                        myMarker.remove();
                    }
                    MarkerOptions marker = new MarkerOptions()
                            .position(loc)
                            .icon(mMarkerIcon);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(origin)
                            .icon(destinationMarkerIcon)
                            .title(Duration);


                    googleMap.addMarker(markerOptions).showInfoWindow();
                    myMarker = googleMap.addMarker(marker);

                    if (!isAnimated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, DEFAULT_ZOOM_LEVEL));
                        isAnimated = true;
                    }
                }
            }
        }
    };

    private void showPlayLocation(Location location) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        Log.v("Booking", "Real Time : lat:" + location.getLatitude() + " longitude:" + location.getLongitude());
        if (googleMap != null) {
            if (myMarker != null) {
                myMarker.remove();
            }
            MarkerOptions marker = new MarkerOptions()
                    .position(loc)
                    .icon(mMarkerIcon);
            myMarker = googleMap.addMarker(marker);
            myMarker.showInfoWindow();

            // set zoom level to show marker
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, DEFAULT_ZOOM_LEVEL));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationUtility locationUtility = LocationUtility.getInstance(getApplicationContext());
        if (locationUtility.isGPSEnabled()) {
            Location location = locationUtility.getLastKnownLocation();
            if (location != null) {
                myLocation = location;
                Log.i(TAG, "onStart Location:" + myLocation.getLatitude() + " : " + myLocation.getLongitude());
            }
        } else {
            locationUtility.showSettingsAlert(MapActivity.this);
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (myLocation != null) {
            showPlayLocation(myLocation);
            Log.i(TAG, "onConnected Location:" + myLocation.getLatitude() + " : " + myLocation.getLongitude());
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}