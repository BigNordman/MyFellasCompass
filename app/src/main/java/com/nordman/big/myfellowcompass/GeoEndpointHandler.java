package com.nordman.big.myfellowcompass;

import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

/**
 * Created by s_vershinin on 21.03.2016.
 *
 */
public interface GeoEndpointHandler {
    int WAKEUP_ERROR = 1;
    int INSERT_ERROR = 2;
    int GET_ERROR = 3;

    void onGeoWakeUp(String hello); // Метод для "поднятия" бэкэнда
    void onGeoInsert(GeoBean geoBean);
    void onGeoGet(GeoBean geoBean);
    void onGeoError(int errorType, String errorMessage);
}
