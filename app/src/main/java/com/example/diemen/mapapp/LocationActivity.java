package com.example.diemen.mapapp;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class LocationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Anuj MapActivity";
    protected GoogleApiClient mGoogleApiClient;
    private AutoCompleteTextView mAutocompleteView;
    private AutoCompleteAdapter mAdapter;
    private TextView mPlaceDetailsText;
    private TextView mselectedLocation;
    private Button okButton;
    Button savedButton;
    private PlaceBuffer selectedPlaces;
    private LatLng latlng;
    private String name;
    private String address;
    DbHandler db = new DbHandler(this);
    int flag = 0;
    Button pickButton;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connecting GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        if (mGoogleApiClient.isConnected())
            Toast.makeText(this, "Anuj is not connected", Toast.LENGTH_SHORT).show();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        setContentView(R.layout.activity_main);
        mselectedLocation = (TextView) findViewById(R.id.textview_selectedlocation);

        //AutoComplete for the Location
        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new AutoCompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteView.setAdapter(mAdapter);

        //Pick Button
        pickButton = (Button) findViewById(R.id.buttonPick);
        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getApplicationContext()), 25);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.d("PlacesAPI Demo", "GooglePlayServicesRepairableException thrown");
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.d("PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown");
                }
            }
        });

        //saved location
        savedButton = (Button) findViewById(R.id.buttonSaved);
        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, SavedActivity.class);
                startActivityForResult(intent, 24);
            }
        });

        //Go back to main Activity
        okButton = (Button) findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPlaces != null) {
                    Intent output = new Intent();
                    final Place place = selectedPlaces.get(0);

                    //Save  the object
                    if (flag != 1) {
                        LocationModel model = new LocationModel();
                        model.lat = String.valueOf(place.getLatLng().latitude);
                        model.lng = String.valueOf(place.getLatLng().longitude);
                        model.name = place.getName().toString();
                        model.address = place.getAddress().toString();
                        db.insertLoc(model);
                    }

                    output.putExtra("latitude", place.getLatLng().latitude + "");
                    output.putExtra("longitude", place.getLatLng().longitude + "");
                    setResult(RESULT_OK, output);
                    selectedPlaces.release();
                    finish();
                } else if (latlng != null) {
                    //Save  the object
                    if (flag != 1) {
                        LocationModel model = new LocationModel();
                        model.lat = String.valueOf(latlng.latitude);
                        model.lng = String.valueOf(latlng.longitude);
                        model.name = name;
                        model.address = address;
                        db.insertLoc(model);
                    }

                    Intent output = new Intent();
                    output.putExtra("latitude", latlng.latitude + "");
                    output.putExtra("longitude", latlng.longitude + "");
                    setResult(RESULT_OK, output);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter a Location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 25 && resultCode == RESULT_OK) {
            displayPlace(PlacePicker.getPlace(data, this));
        }

        if (requestCode == 24 && resultCode == RESULT_OK) {

            latlng = new LatLng(Double.parseDouble(data.getStringExtra("latitude")), Double.parseDouble(data.getStringExtra("longitude")));
            address = data.getStringExtra("address");
            name = data.getStringExtra("name");
            flag = 1;
            //Set Content
            String content = "";
            if (!TextUtils.isEmpty(name)) {
                content += "Name: " + name + "\n";
            }
            if (!TextUtils.isEmpty(address)) {
                content += "Address: " + address + "\n";
            }
            if (latlng != null) {
                content += "Location: " + latlng.toString();
            }
            mselectedLocation.setText(content);
        }
    }

    private void displayPlace(Place place) {
        if (place == null)
            return;

        latlng = place.getLatLng();
        name = place.getName().toString();
        address = place.getAddress().toString();

        String content = "";
        if (!TextUtils.isEmpty(place.getName())) {
            content += "Name: " + place.getName() + "\n";
        }
        if (!TextUtils.isEmpty(place.getAddress())) {
            content += "Address: " + place.getAddress() + "\n";
        }
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            content += "Phone: " + place.getPhoneNumber();
        }

        if (place.getLatLng() != null) {
            content += "Location: " + place.getLatLng().toString();
        }

        mselectedLocation.setText(content);
        Toast.makeText(this, "" + content, Toast.LENGTH_LONG).show();
    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);
            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);


            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            selectedPlaces = places;

            String content = "";
            if (!TextUtils.isEmpty(place.getName())) {
                content += "Name: " + place.getName() + "\n";
            }
            if (!TextUtils.isEmpty(place.getAddress())) {
                content += "Address: " + place.getAddress() + "\n";
            }
            if (!TextUtils.isEmpty(place.getPhoneNumber())) {
                content += "Phone: " + place.getPhoneNumber();
            }

            if (place.getLatLng() != null) {
                content += "Location: " + place.getLatLng().toString();
            }

            mselectedLocation.setText(content);

            Log.i(TAG, "Place details received: " + place.getName());

            //places.release();
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


}
