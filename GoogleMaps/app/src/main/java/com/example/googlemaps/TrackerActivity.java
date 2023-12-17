package com.example.googlemaps;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.googlemaps.model.LocationSingleton;

import java.util.List;

public class TrackerActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        listView = findViewById(R.id.listView);

        LocationSingleton singleton = (LocationSingleton)getApplicationContext();
        List<Location> locations = singleton.getMyLocations();
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations));
    }
}