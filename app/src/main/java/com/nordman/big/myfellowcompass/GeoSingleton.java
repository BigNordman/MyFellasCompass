package com.nordman.big.myfellowcompass;

/**
 * Created by Sergey on 21.04.2016.
 *
 */
public class GeoSingleton {
    private static GeoSingleton ourInstance = null;

    private GeoEndpointManager geoEndpointManager = null;
    private GeoGPSManager geoGPSManager = null;
    private PersonBearingManager personBearingManager = null;

    public static GeoSingleton getInstance() {
        if(ourInstance == null)
        {
            ourInstance = new GeoSingleton();
        }
        return ourInstance;
    }

    private GeoSingleton() {
    }

    public GeoEndpointManager getGeoEndpointManager() {
        return geoEndpointManager;
    }

    public void setGeoEndpointManager(GeoEndpointManager geoEndpointManager) {
        this.geoEndpointManager = geoEndpointManager;
    }

    public GeoGPSManager getGeoGPSManager() {
        return geoGPSManager;
    }

    public void setGeoGPSManager(GeoGPSManager geoGPSManager) {
        this.geoGPSManager = geoGPSManager;
    }

    public PersonBearingManager getPersonBearingManager() {
        return personBearingManager;
    }

    public void setPersonBearingManager(PersonBearingManager personBearingManager) {
        this.personBearingManager = personBearingManager;
    }
}