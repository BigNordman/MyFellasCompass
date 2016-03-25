package com.nordman.big.myfellowcompass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;


import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;
import android.location.LocationListener;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements GeoEndpointHandler {
    public static final long UPDATE_BACKEND_INTERVAL = 15000;


    boolean endpointAlive = false;
    long lastUpdateBackendTime = 0;
    String gpsProvider;
    String criticalErr = null;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    private GeoEndpointManager endpointMgr = null;
    private GeoGPSManager gpsMgr = null;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LOG", "...onCreate...");
        super.onCreate(savedInstanceState);

        /* facebook login */
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("LOG", "login successfull");
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                        Log.d("LOG", "login cancelled");
                        showAlert();
                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("LOG", "Login error" + exception.getLocalizedMessage());
                        showAlert();
                        updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });

        setContentView(R.layout.activity_main);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                Log.d("LOG", "...CurrentProfileChanged...");
                updateUI();
            }
        };

        /* endpoint manager */
        if (endpointMgr == null) {
            Log.d("LOG", "...создаем GeoEndpointManager");
            endpointMgr = new GeoEndpointManager(this);
        }

        /* gps manager */
        if (gpsMgr == null) {
            Log.d("LOG", "...создаем GeoGPSManager");
            gpsMgr = new GeoGPSManager();
        }

        /* location manager */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria crta = new Criteria();
        crta.setAccuracy(Criteria.ACCURACY_FINE);
        crta.setAltitudeRequired(false);
        crta.setBearingRequired(false);
        crta.setCostAllowed(true);
        crta.setPowerRequirement(Criteria.POWER_LOW);
        gpsProvider = locationManager.getBestProvider(crta, true);
        Log.d("LOG","...Provider = " + gpsProvider + "...");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            criticalErr = getString(R.string.AccessFineLocationRequired);
            updateUI();
        } else {
            Location location = locationManager.getLastKnownLocation(gpsProvider);
            updateWithNewLocation(location);

            locationManager.requestLocationUpdates(gpsProvider, 1000, 0, locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            criticalErr = "Provider " + provider + " disabled.";
            updateUI();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    };

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            if (endpointAlive) {

                // update backend not often than 30 second intervals
                long currentTime = new Date().getTime();

                if ((currentTime - lastUpdateBackendTime) > UPDATE_BACKEND_INTERVAL) {
                    Profile profile = Profile.getCurrentProfile();
                    GeoBean geo = new GeoBean();

                    geo.setId(Long.valueOf(profile.getId()));
                    geo.setLat(location.getLatitude());
                    geo.setLon(location.getLongitude());
                    endpointMgr.saveGeo(geo);

                    gpsMgr.setCurrentLocation(location);
                    updateUI();

                    lastUpdateBackendTime = currentTime;
                }

            }
            Log.d("LOG", "...onLocationChanged...");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        endpointMgr.wakeUp();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        Log.d("LOG", "...onDestroy...");
        super.onDestroy();

        AppEventsLogger.deactivateApp(this);

        profileTracker.stopTracking();
        endpointMgr.destroy();

        if (locationListener != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
        //locationListener
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("LOG", "...onStart...");
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("LOG", "onStop...");
    }


    private void updateUI() {
        TextView info = (TextView)findViewById(R.id.textInfo);
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            info.setText(String.valueOf(profile.getId()) + ": " + profile.getFirstName());
        } else {
            info.setText(null);
        }

        TextView textProvider = (TextView)findViewById(R.id.textGPSProvider);
        textProvider.setText(gpsProvider);

        TextView gps = (TextView)findViewById(R.id.textGPS);
        if (criticalErr!=null) {
            gps.setText(criticalErr);
            gps.setTextColor(Color.RED);
        } else {
            gps.setText(getString(R.string.gps_diagnostics, String.format("%.2f", gpsMgr.getDistance()), String.valueOf(gpsMgr.getTime()), String.format("%.2f", gpsMgr.getSpeed()), String.format("%.0f", gpsMgr.getBearing())));
            gps.setTextColor(Color.BLACK);
        }
    }

    @Override
    public void onGeoWakeUp(String hello) {
        endpointAlive = true;
        Toast.makeText(this, hello, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGeoInsert(GeoBean geoBean) {
        Log.d("LOG", "geoBean saved: " + geoBean.toString());
    }

    @Override
    public void onGeoGet(GeoBean geoBean) {
        Log.d("LOG", "geoBean: " + geoBean.toString());
    }

    @Override
    public void onGeoError(int errorType, String errorMessage) {
        if (errorType==GeoEndpointHandler.WAKEUP_ERROR) {
            endpointAlive = false;
        }
        Log.d("LOG",errorMessage);

    }

    public void trySomething(View view) {
        /*
        Profile profile = Profile.getCurrentProfile();
        GeoBean geo = new GeoBean();

        geo.setId(Long.valueOf(profile.getId()));
        geo.setLat(100.001);
        geo.setLon(100.002);
        endpointMgr.saveGeo(geo);

        endpointMgr.getGeo("1");
        */
    }
}
