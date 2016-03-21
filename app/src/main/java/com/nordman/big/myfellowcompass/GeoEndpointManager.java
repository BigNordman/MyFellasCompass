package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.os.AsyncTask;

import com.facebook.appevents.AppEventsLogger;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.GeoBeanApi;

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
                    //return geoApiService.sayHi(name).execute().getExtra();
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

    public void destroy(){
    }
}
