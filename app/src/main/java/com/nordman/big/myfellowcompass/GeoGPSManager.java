package com.nordman.big.myfellowcompass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Date;

/**
 * Created by s_vershinin on 25.03.2016.
 *
 */
public class GeoGPSManager {
    private Context context;
    private LocationManager locationManager;
    private String gpsProvider;
    private TimeLocation prevTimeLoc = null;
    private TimeLocation curTimeLoc = null;

    public GeoGPSManager(Context context) {
        this.context = context;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setAltitudeRequired(false);
        crta.setBearingRequired(false);
        crta.setCostAllowed(true);
        crta.setPowerRequirement(Criteria.POWER_LOW);
        gpsProvider = locationManager.getBestProvider(crta, true);
        Log.d("LOG", "...GPSProvider = " + gpsProvider + "...");

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ((GeoGPSHandler) context).onGPSError(GeoGPSHandler.PERMISSION_ERROR, context.getString(R.string.access_fine_location_required));
        } else {
            Location location = locationManager.getLastKnownLocation(gpsProvider);
            ((GeoGPSHandler) context).onGPSLocationChanged(location);

            locationManager.requestLocationUpdates(gpsProvider, 1000, 0, locationListener);
        }

    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            ((GeoGPSHandler) context).onGPSLocationChanged(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            ((GeoGPSHandler) context).onGPSError(GeoGPSHandler.PROVIDER_DISABLED_ERROR, context.getString(R.string.gps_provider_disabled, provider));
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    class TimeLocation {
        public long time;
        public Location location;

        public TimeLocation(Location location, long time) {
            this.location = location;
            this.time = time;
        }
    }

    public void setCurrentLocation(Location loc) {
        prevTimeLoc = curTimeLoc;
        curTimeLoc = new TimeLocation(loc,new Date().getTime());
    }

    public Location getCurrentLocation() {
        if (curTimeLoc == null) return null;
        else return curTimeLoc.location;
    }

    // distance in meters between measures
    public float getDistance(){
        if (prevTimeLoc != null && curTimeLoc!=null)
            return prevTimeLoc.location.distanceTo(curTimeLoc.location);
        else
            return 0;
    }

    // time in seconds between measures
    public long getTime(){
        if (prevTimeLoc != null && curTimeLoc!=null)
            return (curTimeLoc.time - prevTimeLoc.time)/1000;
        else
            return 0;
    }

    public double getSpeed(){
        long time = getTime();
        if (time!=0) return getDistance()/time*3.6;
        return 0;
    }

    public float getBearing(){
        float bearing;
        if (prevTimeLoc != null && curTimeLoc!=null) {
            bearing = prevTimeLoc.location.bearingTo(curTimeLoc.location);
            return (bearing > 0) ? bearing : (360 + bearing);
        }
        return 0;
    }

    public String getGPSProvider(){
        return gpsProvider;
    }

    public void stopLocating(){
        if (locationListener != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(locationListener);
        }

    }
}
