package com.nordman.big.myfellowcompass;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by s_vershinin on 20.04.2016.
 *
 */
public class PersonOnMap implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private Double lat;
    private Double lon;
    private Double prevLat = 0d;
    private Double prevLon = 0d;

    public PersonOnMap(String id, String name) {
        this.id = id;
        this.name = name;
        this.lat = null;
        this.lon = null;
    }

    public PersonOnMap(String id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLocation(Location location) {
        this.prevLat = lat;
        this.prevLon = lon;
        this.lat = location.getLatitude();
        this.lon= location.getLongitude();
    }

    public boolean isMoved() {
        return !((prevLat == lat) && (prevLon == lon));
    }
}
