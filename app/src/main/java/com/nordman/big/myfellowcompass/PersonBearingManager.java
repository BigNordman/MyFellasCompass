package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

/**
 * Created by s_vershinin on 14.04.2016.
 *
 */
public class PersonBearingManager {
    private Context context;
    private String personId;
    private GeoBean geoBean;
    private Float azimuthDegree = 0f;

    public PersonBearingManager(Context context) {
        this.context = context;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setGeoBean(GeoBean geoBean) {
        this.geoBean = geoBean;
    }

    public float getAzimuthDegree(){

        return azimuthDegree;
    }

    public float getDistance(){
        Location myLocation = getMyLocation();
        Location personLocation = getPersonLocation();

        if (myLocation == null || personLocation == null) return -1;

        return myLocation.distanceTo(personLocation);
    }

    private Location getMyLocation() {
        Location result;
        GeoGPSManager gpsMgr;
        gpsMgr = ((GeoManageable)context).getGeoGPSManager();

        result = gpsMgr.getCurrentLocation();
        if (result != null) Log.d("LOG","...My location = " + result.toString());
        return result;
    }

    private Location getPersonLocation() {
        Location result;
        if (geoBean == null) return null;

        result = new Location("");
        result.setLatitude(geoBean.getLat());
        result.setLongitude(geoBean.getLon());

        Log.d("LOG","...Person location = " + result.toString());

        return result;
    }


    public float[] getRotateDegrees() {
        float[] result;
        result = new float[2];

        return result;
    }
}
