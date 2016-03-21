package com.nordman.big.myfellowcompass;

/**
 * Created by s_vershinin on 21.03.2016.
 *
 */
public interface GeoEndpointHandler {
    int WAKEUP_ERROR = 1;

    void onGeoWakeUp(String hello); // Метод для "поднятия" бэкэнда
    void onGeoError(int errorType, String errorMessage);
}
