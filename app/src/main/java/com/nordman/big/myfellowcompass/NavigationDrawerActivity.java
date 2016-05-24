package com.nordman.big.myfellowcompass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.blunderer.materialdesignlibrary.handlers.ActionBarDefaultHandler;
import com.blunderer.materialdesignlibrary.handlers.ActionBarHandler;
import com.blunderer.materialdesignlibrary.handlers.NavigationDrawerAccountsHandler;
import com.blunderer.materialdesignlibrary.handlers.NavigationDrawerAccountsMenuHandler;
import com.blunderer.materialdesignlibrary.handlers.NavigationDrawerBottomHandler;
import com.blunderer.materialdesignlibrary.handlers.NavigationDrawerStyleHandler;
import com.blunderer.materialdesignlibrary.handlers.NavigationDrawerTopHandler;
import com.blunderer.materialdesignlibrary.models.Account;
import com.facebook.appevents.AppEventsLogger;
import com.nordman.big.myfellowcompass.backend.geoBeanApi.model.GeoBean;

public class NavigationDrawerActivity extends com.blunderer.materialdesignlibrary.activities.NavigationDrawerActivity
        implements GeoEndpointHandler, GeoGPSHandler{

    ViewMapFragment mapFragment;
    public ViewCompassFragment compassFragment;

    private boolean isModalDialogOpened = false;

    @Override
    public void performNavigationDrawerItemClick(int position) {
        super.performNavigationDrawerItemClick(position);
        Log.d("LOG","DrawerItemClick - " + position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* GeoSingleton */

        /* endpoint manager */
        if (GeoSingleton.getInstance().getGeoEndpointManager() == null) {
            GeoSingleton.getInstance().setGeoEndpointManager(new GeoEndpointManager(this));
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

    }

    @Override
    protected boolean enableActionBarShadow() {
        return false;
    }

    @Override
    protected ActionBarHandler getActionBarHandler() {
        return new ActionBarDefaultHandler(this);
    }

    @Override
    public NavigationDrawerStyleHandler getNavigationDrawerStyleHandler() {
        return null;
    }

    @Override
    public NavigationDrawerAccountsHandler getNavigationDrawerAccountsHandler() {
        return new NavigationDrawerAccountsHandler(this)
                .enableSmallAccountsLayout()
                .addAccount(GeoSingleton.getInstance().getProfileName(), "", "https://graph.facebook.com/" + GeoSingleton.getInstance().getProfileId() + "/picture?type=normal", R.drawable.profile_background);
    }

    @Override
    public NavigationDrawerAccountsMenuHandler getNavigationDrawerAccountsMenuHandler() {
        return null;
    }

    @Override
    public void onNavigationDrawerAccountChange(Account account) {

    }

    @Override
    public NavigationDrawerTopHandler getNavigationDrawerTopHandler() {
        mapFragment = new ViewMapFragment();
        compassFragment = new ViewCompassFragment();
        return new NavigationDrawerTopHandler(this)
            .addItem(R.string.fragment_map, mapFragment)
            .addItem(R.string.fragment_compass, compassFragment)
            .addDivider()
            .addItem(R.string.invite_friend,
                        new Intent(getApplicationContext(), InviteFriendActivity.class));
    }

    @Override
    public NavigationDrawerBottomHandler getNavigationDrawerBottomHandler() {
        return new NavigationDrawerBottomHandler(this)
                .addHelpAndFeedback(null);
    }

    @Override
    public boolean overlayActionBar() {
        return false;
    }

    @Override
    public boolean replaceActionBarTitleByNavigationDrawerItemTitle() {
        return false;
    }

    @Override
    public int defaultNavigationDrawerItemSelectedPosition() {
        return 0;
    }


    @Override
    public void onGeoEndpointWakeUp(String hello) {

    }

    @Override
    public void onGeoEndpointInsert(GeoBean geoBean) {

    }

    @Override
    public void onGeoEndpointGet(GeoBean geoBean) {
        if (GeoSingleton.getInstance().getPersonBearingManager()!=null) {
            GeoSingleton.getInstance().getPersonBearingManager().setGeoBean(geoBean);
            Log.d("LOG", "geoBean: " + geoBean.toString());

            if (geoBean.getLat()==null || geoBean.getLon()==null) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        R.string.location_banned_by_friend, Toast.LENGTH_LONG);
                toast.show();
            }


            mapFragment.showHimOnMap();
            compassFragment.setPersonInfo();
        }
    }

    @Override
    public void onGeoEndpointError(int errorType, String errorMessage) {

    }

    @Override
    public void onGPSError(int errorType, String errorMessage) {

    }

    @Override
    public void onGPSLocationChanged(Location location) {
        mapFragment.onGPSLocationChanged(location);
        compassFragment.onGPSLocationChanged(location);
    }

    @Override
    public void onNoPosition() {
        // what if GPS coorinates are unknown
        if (isModalDialogOpened) return;

        AlertDialog.Builder ad = new AlertDialog.Builder(this);

        ad.setTitle(R.string.coordinates_are_unknown);  // заголовок
        ad.setMessage(R.string.please_check_gps); // сообщение
        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                GeoSingleton.getInstance().getGeoGPSManager().stopLocating();
                GeoSingleton.getInstance().getGeoGPSManager().startLocating();
                NavigationDrawerActivity.this.isModalDialogOpened = false;
            }
        });
        ad.setCancelable(false);
        isModalDialogOpened = true;
        ad.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        GeoSingleton.getInstance().startTimer();
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppEventsLogger.deactivateApp(this);
        GeoSingleton.getInstance().clear();
        Log.d("LOG","...onDestroy...");
    }

}
