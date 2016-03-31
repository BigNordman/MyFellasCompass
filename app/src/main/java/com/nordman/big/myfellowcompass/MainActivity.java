package com.nordman.big.myfellowcompass;

import android.app.AlertDialog;
import android.content.Intent;

import android.graphics.Color;
import android.location.Location;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import com.facebook.login.widget.ProfilePictureView;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements GeoEndpointHandler, GeoGPSHandler {
    public static final long UPDATE_BACKEND_INTERVAL = 15000;

    private ProfilePictureView profilePictureView;

    boolean endpointAlive = false;
    long lastUpdateBackendTime = 0;
    String gpsProvider;
    String criticalErr = null;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    private GeoEndpointManager endpointMgr = null;
    private GeoGPSManager gpsMgr = null;
    private MagnetSensorManager magnetManager;

    private ImageView imagePerson;

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
            endpointMgr = new GeoEndpointManager(this);
            Log.d("LOG", "...GeoEndpointManager created...");
        }

        /* gps manager */
        if (gpsMgr == null) {
            gpsMgr = new GeoGPSManager(this);
            Log.d("LOG", "...GeoGPSManager created...");
        }

        /* profile picture view*/
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);

        /* another person picture*/
        imagePerson = (ImageView) findViewById(R.id.imageViewPerson);

        imagePerson.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "TODO: select person", Toast.LENGTH_LONG).show();
            }
        });

        /* magnet manager */
        if (magnetManager == null) {
            Log.d("LOG", "...создаем MagnetSensorManager");
            magnetManager = new MagnetSensorManager(this);
        }

    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            if (endpointAlive) {

                // update backend not often than 30 second intervals
                long currentTime = new Date().getTime();

                if ((currentTime - lastUpdateBackendTime) > UPDATE_BACKEND_INTERVAL) {
                    Profile profile = Profile.getCurrentProfile();
                    if (profile != null) {
                        GeoBean geo = new GeoBean();

                        geo.setId(Long.valueOf(profile.getId()));
                        geo.setLat(location.getLatitude());
                        geo.setLon(location.getLongitude());
                        endpointMgr.saveGeo(geo);
                    }
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
        magnetManager.startSensor();

        updateUI();
    }

    @Override
    protected void onDestroy() {
        Log.d("LOG", "...onDestroy...");
        super.onDestroy();

        AppEventsLogger.deactivateApp(this);

        profileTracker.stopTracking();
        endpointMgr.destroy();
        gpsMgr.stopLocating();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        magnetManager.stopSensor();
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

        Log.d("LOG", "...onStop...");
    }


    private void updateUI() {
        TextView info = (TextView)findViewById(R.id.textInfo);
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            info.setText(getString(R.string.facebook_user_info, String.valueOf(profile.getId()), profile.getFirstName()));
            profilePictureView.setProfileId(profile.getId());
        } else {
            info.setText(null);
            profilePictureView.setProfileId(null);
        }

        TextView textProvider = (TextView)findViewById(R.id.textGPSProvider);
        textProvider.setText(gpsMgr.getGPSProvider());

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
    public void onGeoEndpointWakeUp(String hello) {
        endpointAlive = true;
        Toast.makeText(this, hello, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGeoEndpointInsert(GeoBean geoBean) {
        Log.d("LOG", "geoBean saved: " + geoBean.toString());
    }

    @Override
    public void onGeoEndpointGet(GeoBean geoBean) {
        Log.d("LOG", "geoBean: " + geoBean.toString());
    }

    @Override
    public void onGeoEndpointError(int errorType, String errorMessage) {
        if (errorType==GeoEndpointHandler.WAKEUP_ERROR) {
            endpointAlive = false;
        }
        criticalErr = errorMessage;
        updateUI();
    }

    @Override
    public void onGPSError(int errorType, String errorMessage) {
        criticalErr = errorMessage;
        updateUI();
    }

    @Override
    public void onGPSLocationChanged(Location location) {
        updateWithNewLocation(location);
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
