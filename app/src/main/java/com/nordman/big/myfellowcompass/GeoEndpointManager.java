package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.os.AsyncTask;

import com.facebook.appevents.AppEventsLogger;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.GeoBeanApi;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

import java.io.IOException;

/**
 * Created by s_vershinin on 21.03.2016.
 *
 */
public class GeoEndpointManager {
    private static GeoBeanApi geoApiService = null;
    private Context context;

    public GeoEndpointManager(Context context) {
        this.context = context;
        GeoBeanApi.Builder builder = new GeoBeanApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                .setRootUrl("https://myfellascompassbackend.appspot.com/_ah/api/");

        geoApiService = builder.build();
    }

    public void wakeUp() {
        class wakeUpAsyncTask extends AsyncTask<String, Void, String>{
            private String errorMessage;

            @Override
            protected String doInBackground(String... params) {
                String name = params[0];

                try {
                    return geoApiService.sayHi(name).execute().getExtra();
                } catch (IOException e) {
                    errorMessage = e.getMessage();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if (result==null) {
                    ((GeoEndpointHandler) context).onGeoError(GeoEndpointHandler.WAKEUP_ERROR, errorMessage);
                } else {
                    ((GeoEndpointHandler) context).onGeoWakeUp(result);
                }
            }

        }

        new wakeUpAsyncTask().execute("BigNordman");

    }

    public void saveGeo(GeoBean geoBean) {
        class saveGeoAsyncTask extends AsyncTask<GeoBean, Void, GeoBean> {
            private String errorMessage;

            @Override
            protected GeoBean doInBackground(GeoBean... params) {
                GeoBean geoBean = params[0];

                try {
                    return geoApiService.insert(geoBean).execute();
                    //return geoBean;
                } catch (IOException e) {
                    errorMessage = e.getMessage();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GeoBean geoBean) {
                if (geoBean==null) {
                    ((GeoEndpointHandler) context).onGeoError(GeoEndpointHandler.INSERT_ERROR, errorMessage);
                } else {
                    ((GeoEndpointHandler) context).onGeoInsert(geoBean);
                }
            }
        }

        new saveGeoAsyncTask().execute(geoBean);
    }

    public void getGeo(String id) {
        class getGeoAsyncTask extends AsyncTask<String, Void, GeoBean>{
            private String errorMessage;

            @Override
            protected GeoBean doInBackground(String... params) {
                Long id = Long.parseLong(params[0]);

                try {
                    return geoApiService.get(id).execute();
                } catch (IOException e) {
                    errorMessage = e.getMessage();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(GeoBean result) {
                if (result==null) {
                    ((GeoEndpointHandler) context).onGeoError(GeoEndpointHandler.GET_ERROR, errorMessage);
                } else {
                    ((GeoEndpointHandler) context).onGeoGet(result);
                }
            }

        }

        new getGeoAsyncTask().execute(id);
    }

    public void destroy(){
    }
}
