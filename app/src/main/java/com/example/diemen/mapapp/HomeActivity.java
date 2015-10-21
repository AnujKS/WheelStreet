package com.example.diemen.mapapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by anujkumars on 10/18/2015.
 */
public class HomeActivity extends AppCompatActivity {

    Button locationButton;
    Button  trackButton;
    String deslat;
    String deslng;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Track location from the delivery point
        trackButton=(Button)findViewById(R.id.button_track);
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPlayServices()) {
                    if (deslat != null) {
                        Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                        intent.putExtra("desLat", deslat);
                        intent.putExtra("desLng", deslng);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please Enter Delivery Location", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please update google PlayServices", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Set delivery Location
        locationButton=(Button)findViewById(R.id.button_delivery);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPlayServices()) {
                    Intent intent = new Intent(HomeActivity.this, LocationActivity.class);
                    startActivityForResult(intent, 1);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please update google PlayServices", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( requestCode == 1 && resultCode == RESULT_OK ) {
           deslat=data.getStringExtra("latitude");
           deslng=data.getStringExtra("longitude");
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        // When Play services not found in device
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // Show Error dialog to install Play services
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "This device doesn't support Play services, App will not work normally",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "This device supports Play services, App will work normally",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }

}
