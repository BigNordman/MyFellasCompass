package com.nordman.big.myfellowcompass;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.GeoBeanApi;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sergey on 13.06.2016.
 */
public class WidgetProvider extends AppWidgetProvider {
    private RemoteViews remoteViews;
    private ComponentName appWidget;
    private LightGPSManager gpsMgr;
    private LightEndpointManager endpointMgr;
    private boolean isLocated = true;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidget = new ComponentName( context, WidgetProvider.class );
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            setCurLocation(context);
            isLocated = false;
            remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker_checked);

            // асинхронная задача, чтобы через 10 секунд проверить, определены ли координаты
            PauseTask task = new PauseTask();
            //task.execute();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            Intent homeIntent = new Intent(context, SplashActivity.class);
            PendingIntent homePendingIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.homeButton, homePendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }


    private void setCurLocation(Context context) {
        Log.d("LOG","..Widget action here!");
        gpsMgr = new LightGPSManager(context);
        gpsMgr.startLocating();
    }

    private void saveCurLocation(Context context, Location location) {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);

        String profileId = prefs.getString("profileId",null);
        if (profileId != null) {
            //TODO: вызов процедуры сохранения координат
            endpointMgr = new LightEndpointManager(context);
            GeoBean geo = new GeoBean();
            geo.setId(Long.valueOf(profileId));
            geo.setLat(location.getLatitude());
            geo.setLon(location.getLongitude());
            geo.setExtra(String.valueOf(new Date().getTime()));
            if (endpointMgr != null) {
                endpointMgr.saveGeo(geo);
            }
        }
    }

    void onLocated(Location location) {
        String strCoord = String.valueOf(Util.round(location.getLatitude(),3))
                + ", " + String.valueOf(Util.round(location.getLongitude(),3));
        Log.d("LOG","... Coords = " + strCoord);

        appWidget = new ComponentName( gpsMgr.context, WidgetProvider.class );
        remoteViews = new RemoteViews(gpsMgr.context.getPackageName(), R.layout.widget);
        //remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker);
        remoteViews.setTextViewText(R.id.homeButton,strCoord);
        (AppWidgetManager.getInstance(gpsMgr.context)).updateAppWidget( appWidget, remoteViews );

        gpsMgr.stopLocating();
        Log.d("LOG","... выключили локатинг...");

        saveCurLocation(gpsMgr.context, location);
        gpsMgr = null;
    }

    void onSaved(GeoBean geoBean){

        appWidget = new ComponentName( endpointMgr.context, WidgetProvider.class );
        remoteViews = new RemoteViews(endpointMgr.context.getPackageName(), R.layout.widget);
        remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker);
        (AppWidgetManager.getInstance(endpointMgr.context)).updateAppWidget( appWidget, remoteViews );

        Log.d("LOG","... сохранили местоположение...");
        isLocated = true;
        endpointMgr = null;
    }

    void onSaveError(String errMsg){

        appWidget = new ComponentName( endpointMgr.context, WidgetProvider.class );
        remoteViews = new RemoteViews(endpointMgr.context.getPackageName(), R.layout.widget);
        remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker_err);
        remoteViews.setTextViewText(R.id.homeButton,"");
        (AppWidgetManager.getInstance(endpointMgr.context)).updateAppWidget( appWidget, remoteViews );

        Log.d("LOG",errMsg);
        endpointMgr = null;
    }

    class LightGPSManager {
        private Context context;
        private LocationManager locationManager;
        private String gpsProvider;


        public LightGPSManager(Context context) {
            this.context = context;
        }

        public void startLocating(){
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            Criteria crta = new Criteria();
            crta.setAccuracy(Criteria.ACCURACY_FINE);
            crta.setAltitudeRequired(false);
            crta.setBearingRequired(false);
            crta.setCostAllowed(true);
            crta.setPowerRequirement(Criteria.POWER_LOW);
            gpsProvider = locationManager.getBestProvider(crta, true);
            Log.d("LOG", "...GPSProvider = " + gpsProvider + "...");

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("LOG", "no permissions");
            } else {
                //Location location = locationManager.getLastKnownLocation(gpsProvider);
                //((GeoGPSHandler) context).onGPSLocationChanged(location);
                locationManager.requestLocationUpdates(gpsProvider, 3000, 3, locationListener);
            }
        }

        public void stopLocating(){
            if (locationListener != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(locationListener);
            }

        }

        private final LocationListener locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                //((GeoGPSHandler) context).onGPSLocationChanged(location);
                //setCurrentLocation(location);

                onLocated(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

        };

    }

    class LightEndpointManager {
        private GeoBeanApi geoApiService = null;
        private Context context;

        public LightEndpointManager(Context context) {
            this.context = context;
            GeoBeanApi.Builder builder = new GeoBeanApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(context.getString(R.string.backend_url));

            geoApiService = builder.build();
        }

        public void saveGeo(GeoBean geoBean) {
            class saveGeoAsyncTask extends AsyncTask<GeoBean, Void, GeoBean> {
                private String errorMessage;

                @Override
                protected GeoBean doInBackground(GeoBean... params) {
                    GeoBean geoBean = params[0];


                    try {
                        return geoApiService.insert(geoBean).execute();
                    } catch (IOException e) {
                        errorMessage = e.getMessage();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(GeoBean geoBean) {
                    if (geoBean==null) {
                        onSaveError(errorMessage);
                    } else {
                        onSaved(geoBean);
                    }
                }
            }

            new saveGeoAsyncTask().execute(geoBean);
        }

    }

    // проверяем через 10 секунд после начала определения координат.
    // если координаты неопределены - красим маркер в красное
    class PauseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d("LOG","...10 секунд прошло...isLocated=" + isLocated);
            if (!isLocated) {
                try {
                    appWidget = new ComponentName(gpsMgr.context, WidgetProvider.class);
                    remoteViews = new RemoteViews(gpsMgr.context.getPackageName(), R.layout.widget);
                    remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker_err);
                    (AppWidgetManager.getInstance(gpsMgr.context)).updateAppWidget(appWidget, remoteViews);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
