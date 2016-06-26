package com.nordman.big.myfellowcompass;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

/**
 * Created by Sergey on 21.04.2016.
 *
 */
public class GeoSingleton {
    public static final long TIMER_INTERVAL = 30000;
    private static GeoSingleton ourInstance = null;

    private GeoEndpointManager geoEndpointManager = null;
    private GeoGPSManager geoGPSManager = null;
    private PersonBearingManager personBearingManager = null;
    private String profileId;
    private String profileName;
    private PersonOnMap meOnMap;
    private PersonOnMap himOnMap;
    private int ticksNoPositioning = 0;
    Timer timer = null;

    public static GeoSingleton getInstance() {
        if(ourInstance == null)
        {
            ourInstance = new GeoSingleton();
        }
        return ourInstance;
    }

    public void stopTimer(){
        if (timer !=null) {
            timer.cancel();
            timer = null;
        }
    }

    public void startTimer(){
        if (timer ==null){
            timer = new Timer();
            timer.schedule(new UpdateTimerTask(), 0, TIMER_INTERVAL); //тикаем каждую секунду
            Log.d("LOG","...timer created...");
        }
    }

    private GeoSingleton() {
        startTimer();
    }

    public void clear() {
        if (geoEndpointManager!=null) {
            getGeoEndpointManager().destroy();
            setGeoEndpointManager(null);
        }

        if (geoGPSManager!=null) {
            getGeoGPSManager().destroy();
            setGeoGPSManager(null);
        }

        if (personBearingManager!=null) {
            getPersonBearingManager().destroy();
            setPersonBearingManager(null);
        }
        setMeOnMap(null);
        setHimOnMap(null);
        stopTimer();
    }

    private class UpdateTimerTask extends TimerTask {
        public void run() {
            timerHandler.sendEmptyMessage(0);
        }
    }

    final Handler timerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d("LOG","...timer tick...");

            if (meOnMap != null) {
                if (meOnMap.getLat() == null) {
                    // gps is not on. try to reconnect to GPS
                    geoGPSManager.stopLocating();
                    geoGPSManager.startLocating();
                    ticksNoPositioning++;
                    if (ticksNoPositioning>=2) {    // 1 minute with no positioning
                        // handle this in appropriate screen
                        ticksNoPositioning =0;
                        geoGPSManager.onNoPosition();
                    }
                } else {
                    // meOnMap is correct - with GPS coordinates
                    // so save "me" in cloud
                    GeoBean geo = new GeoBean();
                    geo.setId(Long.valueOf(meOnMap.getId()));
                    geo.setLat(meOnMap.getLat());
                    geo.setLon(meOnMap.getLon());
                    geo.setExtra(String.valueOf(new Date().getTime()));
                    if (geoEndpointManager != null) {
                        geoEndpointManager.saveGeo(geo);
                        Log.d("LOG","...me saved in cloud...");
                    }

                    ticksNoPositioning = 0;
                }
            }

            try {
                getInstance().getGeoEndpointManager().getGeo(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    });

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

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public PersonOnMap getMeOnMap() {
        return meOnMap;
    }

    public void setMeOnMap(PersonOnMap meOnMap) {
        this.meOnMap = meOnMap;
    }

    public PersonOnMap getHimOnMap() {
        return himOnMap;
    }

    public void setHimOnMap(PersonOnMap himOnMap) {
        this.himOnMap = himOnMap;
    }
}
