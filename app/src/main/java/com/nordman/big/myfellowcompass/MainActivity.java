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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
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

import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements GeoEndpointHandler, GeoGPSHandler {
    public static final long UPDATE_BACKEND_INTERVAL = 60000;
    public static final long TICK_INTERVAL = 2000;
    private static final double MIN_SPEED_FOR_ROTATION = 1.2;

    private ProfilePictureView profilePictureView;

    boolean endpointAlive = false;
    long lastUpdateBackendTime = 0;
    String criticalErr = null;

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;

    Timer compassTick = null; // Таймер, использующийся в MainActivity для плавной анимации компаса

    private ProfilePictureView personPictureView;
    private ImageView imageCompass;
    private ImageView imageTriangle;

    //private boolean uiLoaded = false;
    private boolean keepScreenOn = false;
    private boolean initialized = false;

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
        Log.d("LOG", "...setContentView(R.layout.activity_main)...");


        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                Log.d("LOG", "...CurrentProfileChanged...");
                updateUI();
            }
        };

        /* GeoSingleton */

        /* endpoint manager */
        if (GeoSingleton.getInstance().getGeoEndpointManager() == null) {
            GeoSingleton.getInstance().setGeoEndpointManager(new GeoEndpointManager(this));
            //endpointMgr = new GeoEndpointManager(this);
            Log.d("LOG", "...GeoEndpointManager created...");
        }

        /* gps manager */
        if (GeoSingleton.getInstance().getGeoGPSManager() == null) {
            GeoSingleton.getInstance().setGeoGPSManager(new GeoGPSManager(this));
            Log.d("LOG", "...GeoGPSManager created...");
        }

        /* bearing manager */
        if (GeoSingleton.getInstance().getPersonBearingManager() == null) {
            GeoSingleton.getInstance().setPersonBearingManager(new PersonBearingManager(this));
            Log.d("LOG", "...PersonBearingManager created...");
        }

        /* profile picture view*/
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        Log.d("LOG", "...profilePictureView initialized...");

        /* another person picture*/
        personPictureView = (ProfilePictureView) findViewById(R.id.personPicture);

        personPictureView.setOnClickListener(new View.OnClickListener() {

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
        /*
        if (magnetMgr == null) {
            Log.d("LOG", "...создаем MagnetSensorManager");
            magnetMgr = new MagnetSensorManager(this);
        }
        */

        imageCompass = (ImageView) findViewById(R.id.imageViewCompass);
        imageTriangle = (ImageView) findViewById(R.id.imageViewTriangle);
        initialized = true;
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            if (endpointAlive) {

                // update backend not often than 30 second intervals
                long currentTime = new Date().getTime();

                if ((currentTime - lastUpdateBackendTime) > UPDATE_BACKEND_INTERVAL) {
                    // Save "My" coordinates
                    Profile profile = Profile.getCurrentProfile();
                    if (profile != null) {
                        GeoBean geo = new GeoBean();

                        geo.setId(Long.valueOf(profile.getId()));
                        geo.setLat(location.getLatitude());
                        geo.setLon(location.getLongitude());
                        GeoSingleton.getInstance().getGeoEndpointManager().saveGeo(geo);
                    }
                    //gpsMgr.setCurrentLocation(location);

                    // Get "Person" coordinates
                    if (GeoSingleton.getInstance().getPersonBearingManager().getPersonId()!=null)
                        GeoSingleton.getInstance().getGeoEndpointManager().getGeo(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());

                    lastUpdateBackendTime = currentTime;
                }

            }
            if (GeoSingleton.getInstance().getGeoGPSManager() != null) {
                GeoSingleton.getInstance().getGeoGPSManager().setCurrentLocation(location);
            }
            updateUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check on SelectPerson result
        if (requestCode==1) {
            if (data == null) {
                GeoSingleton.getInstance().getPersonBearingManager().setPersonId(null);
                GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.PASSIVE_MODE);
            } else {
                GeoSingleton.getInstance().getPersonBearingManager().setPersonId(data.getStringExtra("id"));
                GeoSingleton.getInstance().getGeoEndpointManager().getGeo(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());

                GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.ACTIVE_MODE);
            }
            updateUI();
        }

        // check on facebook requestCode
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        Log.d("LOG", "...onResume...");
        super.onResume();
        GeoSingleton.getInstance().getGeoEndpointManager().wakeUp();
        //magnetMgr.startSensor();

        // создаем таймер если еще не создан
        if (compassTick ==null){
            compassTick = new Timer();
            compassTick.schedule(new UpdateCompassTickTask(), 0, TICK_INTERVAL); //тикаем каждую секунду
        }

        if (GeoSingleton.getInstance().getPersonBearingManager().getPersonId()!=null)
            GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.ACTIVE_MODE);
        else
            GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.PASSIVE_MODE);


        updateUI();
    }

    @Override
    protected void onDestroy() {
        Log.d("LOG", "...onDestroy...");
        super.onDestroy();

        AppEventsLogger.deactivateApp(this);

        profileTracker.stopTracking();
        GeoSingleton.getInstance().getGeoEndpointManager().destroy();
        GeoSingleton.getInstance().getGeoGPSManager().stopLocating();
    }

    @Override
    protected void onPause() {
        Log.d("LOG", "...onPause...");
        super.onPause();
        // to stop the listener and save battery
        //magnetMgr.stopSensor();
        if (compassTick !=null) {
            compassTick.cancel();
            compassTick = null;
        }

        GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.PASSIVE_MODE);
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
        if (!initialized) return;

        TextView info = (TextView)findViewById(R.id.textInfo);
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            info.setText(getString(R.string.facebook_user_info, String.valueOf(profile.getId()), profile.getFirstName()));
            profilePictureView.setProfileId(profile.getId());
        } else {
            info.setText(null);
            profilePictureView.setProfileId(null);
        }

        TextView gps = (TextView)findViewById(R.id.textGPS);
        if (criticalErr!=null) {
            gps.setText(criticalErr);
            gps.setTextColor(Color.RED);
        } else {
            Location instantLocation = GeoSingleton.getInstance().getGeoGPSManager().getCurrentLocation();
            if (instantLocation != null) {
                gps.setText(getString(R.string.gps_diagnostics, String.valueOf(instantLocation.getLatitude() + " : " + String.valueOf(instantLocation.getLongitude()))));
                gps.setTextColor(Color.BLACK);
            }
        }

        personPictureView.setProfileId(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());
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
        if (GeoSingleton.getInstance().getPersonBearingManager()!=null)
            GeoSingleton.getInstance().getPersonBearingManager().setGeoBean(geoBean);
        Log.d("LOG", "geoBean: " + geoBean.toString());
    }

    @Override
    public void onGeoEndpointError(int errorType, String errorMessage) {

        switch(errorType) {
            case GeoEndpointHandler.WAKEUP_ERROR:
                endpointAlive = false;
                criticalErr = errorMessage;
                break;
            case GeoEndpointHandler.GET_ERROR:
                //criticalErr = getString(R.string.buddy_location_unknown);
                criticalErr = errorMessage;
                break;
        }

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
        Log.d("LOG","...onGPSLocationChanged...");
    }

    private class UpdateCompassTickTask extends TimerTask {
        public void run() {
            compassTickHandler.sendEmptyMessage(0);
        }
    }

    final Handler compassTickHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            /* Magnet compass*/
            /*
            RotateAnimation raMagnet;
            float[] degrees = magnetMgr.getRotateDegrees();
            raMagnet = new RotateAnimation(
                    degrees[0],
                    degrees[1],
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            raMagnet.setDuration(TICK_INTERVAL);
            raMagnet.setFillAfter(true);
            imageCompass.startAnimation(raMagnet);
            */

            /*GPS compass*/
            double curSpeed = GeoSingleton.getInstance().getGeoGPSManager().getSpeed();
            Log.d("LOG","...curSpeed=" + curSpeed);
            if (curSpeed < MIN_SPEED_FOR_ROTATION) {
                return false;
            }

            RotateAnimation ra;
            float[] degrees = GeoSingleton.getInstance().getGeoGPSManager().getRotateDegrees();
            ra = new RotateAnimation(
                    degrees[0],
                    degrees[1],
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(TICK_INTERVAL);
            ra.setFillAfter(true);
            imageCompass.startAnimation(ra);

            degrees = GeoSingleton.getInstance().getPersonBearingManager().getRotateDegrees();
            ra = new RotateAnimation(
                    degrees[0],
                    degrees[1],
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(TICK_INTERVAL);
            ra.setFillAfter(true);
            imageTriangle.startAnimation(ra);

            float distanceToPerson = GeoSingleton.getInstance().getPersonBearingManager().getDistance();
            float azimuthToPerson = GeoSingleton.getInstance().getPersonBearingManager().getAzimuthDegree();
            ((TextView)findViewById(R.id.textExtra1)).setText("Dist = " + String.valueOf(distanceToPerson)+" meters. Azimuth = " + String.valueOf(azimuthToPerson) + "°");

            return false;
        }
    });

    public void trySomething(View view) {
        if (keepScreenOn) {
            keepScreenOn = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ((Button)findViewById(R.id.tryButton)).setText("Keep screen on");
        } else {
            keepScreenOn = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            ((Button)findViewById(R.id.tryButton)).setText("Allow device to sleep");
        }
    }

    public void openMap(View view) {
        // для быстрой отрисовки передаем в карту свои координаты
        Profile profile = Profile.getCurrentProfile();
        PersonOnMap me = null;
        Intent intent = new Intent(this, MapsActivity.class);

        if (profile != null) {
            Location curLocation = GeoSingleton.getInstance().getGeoGPSManager().getCurrentLocation();
            if (curLocation != null) {
                me = new PersonOnMap(profile.getId(),GeoSingleton.getInstance().getGeoGPSManager().getCurrentLocation().getLatitude(),GeoSingleton.getInstance().getGeoGPSManager().getCurrentLocation().getLongitude());

                Bundle mBundle = new Bundle();
                mBundle.putSerializable("PersonOnMap", me);
                intent.putExtras(mBundle);
            }
        }

        startActivity(intent);
    }
}
