package com.nordman.big.myfellowcompass;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blunderer.materialdesignlibrary.fragments.AFragment;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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


    public ViewMapFragment() {
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
        Log.d("LOG","...onViewCreated...");

        setUpMapIfNeeded();
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
    }

    public void onGPSLocationChanged(Location location) {
        if (GeoSingleton.getInstance().getMeOnMap() == null) {
            PersonOnMap me = new PersonOnMap(GeoSingleton.getInstance().getProfileId(),GeoSingleton.getInstance().getProfileName());
            me.setLocation(location);
            GeoSingleton.getInstance().setMeOnMap(me);
        } else {
            GeoSingleton.getInstance().getMeOnMap().setLocation(location);
        }
        Log.d("LOG","...onGPSLocationChanged...");

        showMeOnMap();
    }

    private void showMeOnMap() {
        final PersonOnMap me = GeoSingleton.getInstance().getMeOnMap();
        if ((mMap != null) && (me != null)){
            Log.d("LOG","...showMeOnMap...");

            // check if person is moving
            if ( me.isMoved()) {
                if (meMarker == null) {
                    // create new marker with facebook picture
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
                    Log.d("LOG","...create marker...");
                } else {
                    // move existing marker
                    meMarker.setPosition(new LatLng(me.getLat(), me.getLon()));
                    Log.d("LOG","...move marker...");
                }
            } else {
                Log.d("LOG","...person is not moving...");
            }
        }
    }

    private class GetProfileImageTask extends AsyncTask<PersonOnMap, Integer, Bitmap> {
        // Do the long-running work in here
        protected Bitmap doInBackground(PersonOnMap... person) {
            Bitmap result = null;
            URL imgUrl = null;
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
            PersonOnMap me = GeoSingleton.getInstance().getMeOnMap();
            Bitmap roundPict = Util.getCroppedBitmap(result);
            LatLng myLatLng = new LatLng(toDraw.getLat(), toDraw.getLon());

            meMarker = mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title(toDraw.getId())
                    .icon(BitmapDescriptorFactory.fromBitmap(roundPict)));
            if (toDraw == me) {
                Float zoomRate = 16.0f;


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

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomRate));

                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
            }
        }
    }
}
