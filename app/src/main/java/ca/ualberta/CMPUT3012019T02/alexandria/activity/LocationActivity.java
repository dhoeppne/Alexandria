/**
 * Copyright 2014 The Android Open Source Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ca.ualberta.CMPUT3012019T02.alexandria.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import ca.ualberta.CMPUT3012019T02.alexandria.R;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Location mLastKnownLocation;

    private FusedLocationProviderClient mFusedLocationProviderClient;


    // Default location (Edmonton, AB) assuming permissions are denied
    private final LatLng mDefaultLocation = new LatLng(53.5444,-113.4909);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable("location");
            mCameraPosition = savedInstanceState.getParcelable("camera_position");
        }

        setContentView(R.layout.activity_map);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("LOCATION ACTIVITY", "got to onMapReady");
        mMap = googleMap;

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        // check if location permission is accepted or not
        if (mLocationPermissionGranted) {
            // get current location and put a pin
            //LatLng lastPosition = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(lastPosition).draggable(true));
            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).draggable(true));
        } else {
            // set pin to default location
            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).draggable(true));
        }
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                mLastKnownLocation.setLatitude(position.latitude);
                mLastKnownLocation.setLongitude(position.longitude);
            }
        });

        // Save current pin location to the location message
        Button savePin = (Button)findViewById(R.id.button_save_pin);
        savePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Set chat controller method call for location message
                finish();
            }
        });
    }



    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Log.d("LOCATION ACTIVITY",  "Have location permissions, getting device location");
                /*
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                   if (location != null){
                       try {
                           mLastKnownLocation.setLatitude(location.getLatitude());
                           mLastKnownLocation.setLongitude(location.getLongitude());
                           LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                           mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                           Log.d("LOCATION ACTIVITY", "location" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                       } catch (Exception e) {
                           Log.e("Exception:", "location was null");
                       }
                    }
                });
                */

                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d("LOCATION ACTIVITY",  "COMPLETE CURRENT POSITION");
                        if (task.isSuccessful()) {
                            Log.d("LOCATION ACTIVITY",  "GETTING CURRENT POSITION SUCCESS");
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            Log.d("LOCATION ACTIVITY", "location" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                        } else {
                            Log.d("MAP_FRAGMENT", "Current location is null. Using defaults.");
                            Log.e("MAP_FRAGMENT", "Exception: %s", task.getException());
                            mLastKnownLocation.setLatitude(mDefaultLocation.latitude);
                            mLastKnownLocation.setLongitude(mDefaultLocation.longitude);
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });

            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Log.d("LOCATION ACTIVITY", "asking for location permission");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        Log.d("LOCATION ACTIVITY", "updating location ui");
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
