package com.nordman.big.myfellowcompass;

import android.location.Location;

import java.util.Date;

/**
 * Created by s_vershinin on 25.03.2016.
 *
 */
public class GeoGPSManager {
    private TimeLocation prevTimeLoc = null;
    private TimeLocation curTimeLoc = null;

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

}
