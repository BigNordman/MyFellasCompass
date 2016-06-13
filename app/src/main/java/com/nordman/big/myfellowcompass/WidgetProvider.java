package com.nordman.big.myfellowcompass;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.facebook.Profile;

/**
 * Created by Sergey on 13.06.2016.
 */
public class WidgetProvider extends AppWidgetProvider {
    private RemoteViews remoteViews;
    private ComponentName appWidget;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidget = new ComponentName( context, WidgetProvider.class );

        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            setCurLocation(context);
            //remoteViews.

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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        /*
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker_checked);
        appWidget = new ComponentName( context, WidgetProvider.class );
        (AppWidgetManager.getInstance(context)).updateAppWidget( appWidget, remoteViews );
        */
    }


    private void setCurLocation(Context context) {
        Log.d("LOG","..Widget action here!");
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);
        Log.d("LOG",prefs.getString("profileId",null));
        GeoSingleton.getInstance().setProfileId(prefs.getString("profileId",null));
/*
        if (GeoSingleton.getInstance().getProfileId() != null) {
            Log.d("LOG",GeoSingleton.getInstance().getProfileId());
        } else {
            Log.d("LOG","profile is null");
        }
        if (GeoSingleton.getInstance().getGeoEndpointManager() == null) {
            GeoSingleton.getInstance().setGeoEndpointManager(new GeoEndpointManager(context));
            Log.d("LOG", "...GeoEndpointManager created...");
        }

        if (GeoSingleton.getInstance().getGeoGPSManager() == null) {
            GeoSingleton.getInstance().setGeoGPSManager(new GeoGPSManager(context));
            Log.d("LOG", "...GeoGPSManager created...");
        }
*/
        /*
        updateLocationThread updater = new updateLocationThread();
        updater.context = context;
        updater.start();
        */
    }

    private class updateLocationThread extends Thread
    {
        Context context;

        @Override
        public void run() {
            try {
                appWidget = new ComponentName( context, WidgetProvider.class );
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

                Log.d("LOG","..Widget green!");
                remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker_checked);
                (AppWidgetManager.getInstance(context)).updateAppWidget( appWidget, remoteViews );

                Log.d("LOG",GeoSingleton.getInstance().getProfileId());
                Thread.sleep(2000);

                Log.d("LOG","..Widget white!");
                remoteViews.setImageViewResource(R.id.actionButton, R.drawable.btn_marker);
                (AppWidgetManager.getInstance(context)).updateAppWidget( appWidget, remoteViews );


                // Etc.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
