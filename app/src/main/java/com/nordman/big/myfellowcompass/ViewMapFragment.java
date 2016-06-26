package com.nordman.big.myfellowcompass;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.blunderer.materialdesignlibrary.fragments.AFragment;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.SendButton;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ViewMapFragment extends AFragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private PersonOnMap toDraw = null;
    private Marker meMarker = null;
    private Marker himMarker = null;
    private ImageView personSelector = null;
    private static final int SHARE_TO_MESSENGER_REQUEST_CODE = 1;
    private MessengerThreadParams mThreadParams;
    private boolean mPicking;

    public ViewMapFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.mapPersonSelector).setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("LOG","... ViewMapFragment.onActivityResult  requestCode = " + requestCode);
        // check on SelectPerson result
        if (requestCode==1) {
            if (data == null) {

                GeoSingleton.getInstance().getPersonBearingManager().setPersonId(null);
                GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.PASSIVE_MODE);

            } else {
                if (himMarker != null) {
                    himMarker.remove();
                    himMarker = null;
                }
                GeoSingleton.getInstance().getPersonBearingManager().setPersonId(data.getStringExtra("id"));
                GeoSingleton.getInstance().getPersonBearingManager().setPersonName(data.getStringExtra("name"));
                GeoSingleton.getInstance().getGeoEndpointManager().getGeo(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());

                PersonOnMap him = new PersonOnMap(data.getStringExtra("id"),data.getStringExtra("name"));
                GeoSingleton.getInstance().setHimOnMap(him);

                GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.ACTIVE_MODE);

                Log.d("LOG","...person id = " + data.getStringExtra("id"));

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("LOG","...onCreateView...");
        return inflater.inflate(R.layout.fragment_view_map, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        personSelector = (ImageView) getActivity().findViewById(R.id.mapPersonSelector);
        personSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LOG","...select person stub...");
                personSelector.setEnabled(false);
                GraphRequestAsyncTask graphRequestAsyncTask = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                Intent intent = new Intent(getActivity(), SelectPerson2Activity.class);
                                try {
                                    JSONArray rawName = response.getJSONObject().getJSONArray("data");
                                    intent.putExtra("jsondata", rawName.toString());
                                    getActivity().startActivityForResult(intent, 1);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                ).executeAsync();

            }
        });

        ImageView imageCompass = (ImageView)getActivity().findViewById(R.id.mapCompass);
        imageCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NavigationDrawerActivity) getActivity()).performNavigationDrawerItemClick(1);
            }
        });

        Intent intent = this.getActivity().getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;

            // Note, if mThreadParams is non-null, it means the activity was launched from Messenger.
            // It will contain the metadata associated with the original content, if there was content.
        }

        ImageView imageSend = (ImageView)getActivity().findViewById(R.id.mapSend);
        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LOG","...send button pressed...");
                onMessengerButtonClicked();
            }
        });

        setUpMapIfNeeded();
    }

    private void onMessengerButtonClicked() {
        // The URI can reference a file://, content://, or android.resource. Here we use
        // android.resource for sample purposes.
        Uri uri =
                Uri.parse("android.resource://com.nordman.big.myfellowcompass/" + R.drawable.logo);

        // Create the parameters for what we want to send to Messenger.
        ShareToMessengerParams shareToMessengerParams =
                ShareToMessengerParams.newBuilder(uri, "image/jpeg")
                        .setMetaData("{ \"image\" : \"logo\" }")
                        .build();

        if (mPicking) {
            // If we were launched from Messenger, we call MessengerUtils.finishShareToMessenger to return
            // the content to Messenger.
            MessengerUtils.finishShareToMessenger(this.getActivity(), shareToMessengerParams);
        } else {
            // Otherwise, we were launched directly (for example, user clicked the launcher icon). We
            // initiate the broadcast flow in Messenger. If Messenger is not installed or Messenger needs
            // to be upgraded, this will direct the user to the play store.
            MessengerUtils.shareToMessenger(
                    this.getActivity(),
                    SHARE_TO_MESSENGER_REQUEST_CODE,
                    shareToMessengerParams);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMap.clear();
        mMap = null;
        meMarker = null;
        himMarker = null;
        Log.d("LOG","...onDetach...");
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("LOG","...onMapReady...");
        showMeOnMap();
        if (GeoSingleton.getInstance().getPersonBearingManager().getPersonId() != null) {
            if (GeoSingleton.getInstance().getHimOnMap() == null) {
                PersonOnMap him = new PersonOnMap(GeoSingleton.getInstance().getPersonBearingManager().getPersonId(),GeoSingleton.getInstance().getPersonBearingManager().getPersonName());
                GeoSingleton.getInstance().setHimOnMap(him);
            }
            showHimOnMap();
            Log.d("LOG","him!!!");
        }
    }

    public void onGPSLocationChanged(Location location) {
        if (GeoSingleton.getInstance().getMeOnMap() == null) {
            PersonOnMap me = new PersonOnMap(GeoSingleton.getInstance().getProfileId(),GeoSingleton.getInstance().getProfileName());
            me.setLocation(location);
            GeoSingleton.getInstance().setMeOnMap(me);
        } else {
            GeoSingleton.getInstance().getMeOnMap().setLocation(location);
        }
        if (location==null) {
            Log.d("LOG", "...onGPSLocationChanged - location is null...");
        } else {
            Log.d("LOG", "...onGPSLocationChanged...lat=" + location.getLatitude() + " lon = " + location.getLongitude());
        }

        showMeOnMap();
    }


    public void showMeOnMap() {
        final PersonOnMap me = GeoSingleton.getInstance().getMeOnMap();
        if ((mMap != null) && (me != null)){
            Log.d("LOG","...ViewMapFragment.showMeOnMap...");

            if (meMarker == null) {
                // create new marker with facebook picture
                setProgressBarVisibility(View.VISIBLE);
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + me.getId() + "/picture",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                new GetProfileImageTask().execute(me);
                            }
                        }
                ).executeAsync();
                Log.d("LOG","...create me marker...");
            } else {
                // move existing marker
                if ( me.isMoved()) {
                    meMarker.setPosition(new LatLng(me.getLat(), me.getLon()));
                    Log.d("LOG", "...move me marker...");
                }
            }
        }
    }

    public void showHimOnMap() {
        //GeoSingleton.getInstance().getPersonBearingManager().getGeoBean()
        final PersonOnMap him = GeoSingleton.getInstance().getHimOnMap();
        Location personLoc = GeoSingleton.getInstance().getPersonBearingManager().getPersonLocation();
        him.setLocation(personLoc);
        him.setLastTime(GeoSingleton.getInstance().getPersonBearingManager().getPersonLastTime());

        if ((mMap != null) && (him != null)) {
            Log.d("LOG", "...ViewMapFragment.showHimOnMap...");

            if (himMarker == null) {
                // create new marker with facebook picture
                setProgressBarVisibility(View.VISIBLE);
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + him.getId() + "/picture",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                new GetProfileImageTask().execute(him);
                            }
                        }
                ).executeAsync();
                Log.d("LOG","...create him marker...");
            } else {
                if ( him.isMoved()) {
                    // move existing marker
                    himMarker.setPosition(new LatLng(him.getLat(), him.getLon()));
                }
            }
        }

    }

    private class GetProfileImageTask extends AsyncTask<PersonOnMap, Integer, Bitmap> {
        // Do the long-running work in here
        protected Bitmap doInBackground(PersonOnMap... person) {
            Bitmap result = null;
            URL imgUrl;
            try {
                imgUrl = new URL("https://graph.facebook.com/" + person[0].getId() + "/picture?type=normal" );

                InputStream in = (InputStream) imgUrl.getContent();
                toDraw = person[0];
                result = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                setProgressBarVisibility(View.INVISIBLE);
                return;
            }
            PersonOnMap me = GeoSingleton.getInstance().getMeOnMap();
            Bitmap roundPict = Util.getCroppedBitmap(result);

            if (toDraw != me) {
                // draw his picture
                personSelector.setImageBitmap(roundPict);
            }

            if ((toDraw.getLat()==null)||(toDraw.getLon()==null)) {
                // coordinates undefined - do nothing
                return;
            }

            LatLng myLatLng = new LatLng(toDraw.getLat(), toDraw.getLon());
            Float zoomRate = 16.0f;

            if (toDraw == me) {
                Log.d("LOG","...me on map...");

                try {
                    if (meMarker != null) meMarker.remove();
                    meMarker = mMap.addMarker(new MarkerOptions()
                            .position(myLatLng)
                            .title(toDraw.getName())
                            .icon(BitmapDescriptorFactory.fromBitmap(roundPict)));
                    setProgressBarVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
            } else {
                Log.d("LOG","...him on map...");

                try {
                    if (himMarker != null) himMarker.remove();
                    himMarker = mMap.addMarker(new MarkerOptions()
                            .position(myLatLng)
                            .title(toDraw.getName())
                            .snippet(toDraw.getTimePassed(getContext()))
                            .icon(BitmapDescriptorFactory.fromBitmap(roundPict)));
                    setProgressBarVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            try {
                if (GeoSingleton.getInstance().getPersonBearingManager().getPersonId() != null) {
                    float distance = GeoSingleton.getInstance().getPersonBearingManager().getDistance();
                    if (distance < 200) {
                        zoomRate = 16.0f;
                    } else if (distance < 800) {
                        zoomRate = 14.0f;
                    }
                    else if (distance < 3500) {
                        zoomRate = 12.0f;
                    }
                    else if (distance < 15000) {
                        zoomRate = 10.0f;
                    }
                    else if (distance < 60000) {
                        zoomRate = 8.0f;
                    }
                    else if (distance < 220000) {
                        zoomRate = 6.0f;
                    }
                    else if (distance < 750000) {
                        zoomRate = 4.0f;
                    }
                    else {
                        zoomRate = 3.0f;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomRate));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void onTest(View view) {
        Log.d("LOG","...onTest()...");
        /*
        LatLng myLatLng = new LatLng(56.825923, 60.604668);
        mMap.addMarker(new MarkerOptions()
                .position(myLatLng));
        */
    }
    public void onTest2(View view) {
        //mMap.getM
        Log.d("LOG","...onTest2()...");
    }

    private void setProgressBarVisibility(int visibility)
    {
        if (getActivity()==null) return;
        getActivity().findViewById((R.id.imageProgressBar)).setVisibility(visibility);
        getActivity().findViewById((R.id.progressBar)).setVisibility(visibility);
    }

}
