package com.example.googlemaps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.googlemaps.model.LocationSingleton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location locations;
    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    private static final int PERMISSIONS_FINE_LOCATION = 0;
    private static final int DEFAULT_UPDATE_INTERVAL = 30000;
    private static final int FAST_UPDATE_INTERVAL = 5000;

    List<Location> trackedLocations;
    List<Polyline> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        androidx.preference.PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        boolean switchPref1 = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_1, false);
        boolean switchPref2 = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_2, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FAST_UPDATE_INTERVAL);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateLatLng(locationResult.getLastLocation());
            }
        };
        if (switchPref1) {
            startLocationTrack();
            Log.i("LOCATION: ", getString(R.string.locTrackOn));
        } else {
            stopLocationTrack();
            Log.i("LOCATION: ", getString(R.string.lockTrackOff));
        }
        if (switchPref2) {
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.i("LOCATION: ", getString(R.string.GpsOn));
        } else {
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            Log.i("LOCATION: ", getString(R.string.GpsOff));
        }
    }

    private void updateLatLng(Location lastLocation) {
        Log.i("LOCATION: ", lastLocation.getLatitude() + "\n" + lastLocation.getLongitude());
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try{
            List<Address> addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
            Log.i("GEOCODER: ", addresses.get(0).getAddressLine(0));
        } catch (Exception e){
            Log.e("GEOCODER: ", e.getMessage());
        }

        LocationSingleton singleton = (LocationSingleton)getApplicationContext();
        trackedLocations = singleton.getMyLocations();
        trackedLocations.add(lastLocation);
    }

    private void stopLocationTrack() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationTrack() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(currentPosition).title("You're here at the moment!"));
                GroundOverlayOptions locationOverlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.overlay)).position(currentPosition, 100);
                mMap.addGroundOverlay(locationOverlay);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onSettings(MenuItem item) {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

    public void AddPolyline(MenuItem item) {
        // Update the application so that if the user has turned on location tracking
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            PolylineOptions line = new PolylineOptions()
                    .add(new LatLng(locations.getLatitude(), locations.getLongitude()))
                    .color(Color.GREEN)
                    .width(5);
            // then the location points are saved to a list that allows the user to display them on the map with polylines.
            Polyline polyline = mMap.addPolyline(line);
            points.add(polyline);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.noperms), Toast.LENGTH_SHORT).show();
        }
    }
}