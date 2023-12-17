package com.example.googlemaps.model;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationSingleton extends Application {

    private static LocationSingleton singleton;
    private List<Location> myLocations;

    public static LocationSingleton getSingleton() {
        return singleton;
    }

    public static void setSingleton(LocationSingleton singleton) {
        LocationSingleton.singleton = singleton;
    }

    public LocationSingleton getInstance(){return singleton;}

    @Override
    public void onCreate(){
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    }

    public List<Location> getMyLocations() { return myLocations;}
}