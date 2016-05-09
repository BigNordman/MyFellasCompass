package com.nordman.big.myfellowcompass;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    public static final long START_DELAY = 1500;

    private ProfilePictureView profilePictureView;
    private CallbackManager fbCallbackManager;
    private ProfileTracker profileTracker;
    private ImageButton continueButton;
    Timer delayTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* facebook login */
        FacebookSdk.sdkInitialize(getApplicationContext());
        fbCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(fbCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("LOG", "login successfull");
                        updateProfile();
                    }

                    @Override
                    public void onCancel() {
                        Log.d("LOG", getString(R.string.facebook_login_cancelled));
                        showAlert( R.string.cancelled, R.string.facebook_login_cancelled);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d("LOG", getString(R.string.login_error) + exception.getLocalizedMessage());
                        showAlert(R.string.login_error,R.string.something_went_wrong);
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(SplashActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }

                    private void showAlert(int pTitleId, int pMessageId) {
                        new AlertDialog.Builder(SplashActivity.this)
                                .setTitle(pTitleId)
                                .setMessage(pMessageId)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });


        setContentView(R.layout.activity_splash);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");

        continueButton = (ImageButton) findViewById(R.id.continueButton);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateProfile();
                Log.d("LOG", "...CurrentProfileChanged...");
            }
        };

        profilePictureView = (ProfilePictureView) findViewById(R.id.splashProfilePicture);

        updateProfile();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check on facebook requestCode
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateProfile() {
        Profile profile = Profile.getCurrentProfile();

        if (profile != null) {
            profilePictureView.setProfileId(profile.getId());
            continueButton.setVisibility(View.VISIBLE);
            GeoSingleton.getInstance().setProfileId(profile.getId());
            GeoSingleton.getInstance().setProfileName(profile.getName());
        } else {
            profilePictureView.setProfileId(null);
            continueButton.setVisibility(View.INVISIBLE);
            GeoSingleton.getInstance().setProfileId(null);
            GeoSingleton.getInstance().setProfileName(null);
        }
    }

    public void onContinue(View view) {

        if (!Util.isOnline(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.connection_error)
                    .setMessage(R.string.connection_is_not_available)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            continueButton.setVisibility(View.INVISIBLE);
            return;
        }


        Intent intent = new Intent(SplashActivity.this, NavigationDrawerActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private class delayTask extends TimerTask {
        public void run() {
            delayHandler.sendEmptyMessage(0);
        }
    }
    final Handler delayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            delayTimer.cancel();
            delayTimer = null;
            Log.d("LOG","...after sleep...");

            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();

            return false;
        }
    });
}
