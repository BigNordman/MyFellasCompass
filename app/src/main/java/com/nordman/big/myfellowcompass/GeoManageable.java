package com.nordman.big.myfellowcompass;

/**
 * Created by s_vershinin on 14.04.2016.
 * Интерфейс для тех Activity, которые обязаны содержать все менеджеры
 */
public interface GeoManageable {
    GeoEndpointManager getGeoEndpointManager();
    GeoGPSManager getGeoGPSManager();
    //MagnetSensorManager getMagnetSensorManager();
    PersonBearingManager getPersonBearingManager();

}
