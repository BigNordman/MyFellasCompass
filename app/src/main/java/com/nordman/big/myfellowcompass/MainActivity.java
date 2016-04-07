package com.nordman.big.myfellowcompass;

import android.app.AlertDialog;
import android.content.Intent;

import android.graphics.Color;
import android.location.Location;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements GeoEndpointHandler, GeoGPSHandler {
    public static final long UPDATE_BACKEND_INTERVAL = 15000;
    public static final long TICK_INTERVAL = 2000;

    private ProfilePictureView profilePictureView;

    boolean endpointAlive = false;
    long lastUpdateBackendTime = 0;
    String criticalErr = null;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    private GeoEndpointManager endpointMgr = null;
    private GeoGPSManager gpsMgr = null;
    private MagnetSensorManager magnetManager;
    Timer compassTick = null; // Таймер, использующийся в MainActivity для плавной анимации компаса

    private ImageView imagePerson;
    private ImageView imageCompass;


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

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

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
                /*
                Intent intent = new Intent(MainActivity.this, SelectPersonActivity.class);
                MainActivity.this.startActivityForResult(intent, 1);
                */
                GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        //AccessToken.getCurrentAccessToken(),
                        "/me/friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Intent intent = new Intent(MainActivity.this, SelectPersonActivity.class);
                                try {
                                    JSONArray rawName = response.getJSONObject().getJSONArray("data");
                                    intent.putExtra("jsondata", rawName.toString());
                                    //startActivity(intent);
                                    startActivityForResult(intent, 1);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAsync();
            }
        });

        /* magnet manager */
        if (magnetManager == null) {
            Log.d("LOG", "...создаем MagnetSensorManager");
            magnetManager = new MagnetSensorManager(this);
        }

        imageCompass = (ImageView) findViewById(R.id.imageViewCompass);

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

        // check on facebook requestCode
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        endpointMgr.wakeUp();
        magnetManager.startSensor();

        // создаем таймер если еще не создан
        if (compassTick ==null){
            compassTick = new Timer();
            compassTick.schedule(new UpdateCompassTickTask(), 0, TICK_INTERVAL); //тикаем каждую секунду
        }


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
        if (compassTick !=null) {
            compassTick.cancel();
            compassTick = null;
        }

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


    private class UpdateCompassTickTask extends TimerTask {
        public void run() {
            compassTickHandler.sendEmptyMessage(0);
        }
    }

    final Handler compassTickHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            RotateAnimation ra;
            float[] degrees = magnetManager.getRotateDegrees();
            ra = new RotateAnimation(
                    degrees[0],
                    degrees[1],
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(TICK_INTERVAL);
            ra.setFillAfter(true);
            imageCompass.startAnimation(ra);

            return false;
        }
    });

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
