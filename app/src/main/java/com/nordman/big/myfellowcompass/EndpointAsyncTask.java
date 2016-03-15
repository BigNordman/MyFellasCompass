package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.GeoBeanApi;

import java.io.IOException;

/**
 * Created by s_vershinin on 11.03.2016.
 * Асинхронная задача для общения с бэкэндом
 */
public class EndpointAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
    private static GeoBeanApi geoApiService = null;
    private Context context;

    @SafeVarargs
    @Override
    protected final String doInBackground(Pair<Context, String>... params) {
        if(geoApiService == null) {  // Only do this once
            /*
            GeoBeanApi.Builder builder = new GeoBeanApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            */

            GeoBeanApi.Builder builder = new GeoBeanApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl("https://myfellascompassbackend.appspot.com/_ah/api/");
            // end options for devappserver


            geoApiService = builder.build();
        }

        context = params[0].first;
        String name = params[0].second;

        try {
            //return geoApiService.sayHi(name).execute().getExtra();
            return geoApiService.sayHi(name).execute().getExtra();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
    }
}
