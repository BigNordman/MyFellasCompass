package com.nordman.big.myfellowcompass;

import android.location.Location;

/**
 * Created by s_vershinin on 31.03.2016.
 *
 */
public interface GeoGPSHandler {
    int PERMISSION_ERROR = 1;
    int PROVIDER_DISABLED_ERROR = 2;

    void onGPSError(int errorType, String errorMessage);
    void onGPSLocationChanged(Location location);
}
