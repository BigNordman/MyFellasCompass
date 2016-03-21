package com.nordman.big.myfellowcompass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,GeoEndpointHandler {
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean endpointAlive = false;
    GeoEndpointManager geoMgr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                Log.d("LOG","...CurrentProfileChanged...");
                updateUI();
            }
        };

        if (mGoogleApiClient == null) {
            Log.d("LOG","...создаем GooglAPIClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (geoMgr == null) {
            Log.d("LOG","...создаем GeoEndpointManager");
            geoMgr = new GeoEndpointManager(this);
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
        geoMgr.wakeUp();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
        geoMgr.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        endpointAlive = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("LOG", "...onStart...");
        mGoogleApiClient.connect();
        AppEventsLogger.activateApp(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("LOG", "onStop...");
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        AppEventsLogger.deactivateApp(this);

    }


    private void updateUI() {
        TextView info = (TextView)findViewById(R.id.info);
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            info.setText(String.valueOf(profile.getId()) + ": " + profile.getFirstName());
        } else {
            info.setText(null);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LOG", "...onConnected...");
        createLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("LOG", "...onConnectionSuspended...");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("LOG", "...onConnectionFailed...");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (endpointAlive) {
            Log.d("LOG", "...Endpoint Alive...");
        }
        Log.d("LOG", "...onLocationChanged...");
    }

    @Override
    public void onGeoWakeUp(String hello) {
        endpointAlive = true;
        Toast.makeText(this, hello, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGeoError(int errorType, String errorMessage) {
        if (errorType==GeoEndpointHandler.WAKEUP_ERROR) {
            endpointAlive = false;
            Log.d("LOG",errorMessage);
        }
    }
}
