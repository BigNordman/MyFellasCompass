package com.nordman.big.myfellowcompass;

import android.content.Context;
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
import com.google.common.io.Resources;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private long lastTime = 0;

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
        if (location != null) {
            this.lat = location.getLatitude();
            this.lon = location.getLongitude();
        }
    }

    public long getLastTime() {
        return lastTime;
    }

    public String getLastTimeFormatted(){
        DateFormat sdf = DateFormat.getDateTimeInstance();
        return sdf.format(lastTime);
    }

    public String getTimePassed(Context context){
        long seconds = (new Date().getTime() - lastTime)/1000;
        long minutes = (new Date().getTime() - lastTime)/1000/60;
        long hours = (new Date().getTime() - lastTime)/1000/60/60;
        long days = (new Date().getTime() - lastTime)/1000/60/60/24;

        String result = "";
        if (seconds < 60) result = context.getString(R.string.right_now);
        else if (minutes < 60) result = context.getString(R.string.minutes_ago, minutes);
        else if (hours < 24) result = context.getString(R.string.hours_ago, hours);
        else if (days < 30) result = context.getString(R.string.days_ago, days);
        else result = context.getString(R.string.more_tan_month_ago);

        return String.valueOf(result);
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public boolean isMoved() {
        return !((prevLat == lat) && (prevLon == lon));
    }
}
