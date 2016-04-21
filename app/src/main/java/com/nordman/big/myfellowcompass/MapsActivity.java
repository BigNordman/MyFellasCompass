package com.nordman.big.myfellowcompass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.InputStream;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PersonOnMap me = null;
    private PersonOnMap him = null;
    private PersonOnMap toDraw = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        me = (PersonOnMap) intent.getSerializableExtra("PersonOnMap");

        if (GeoSingleton.getInstance().getPersonBearingManager().getPersonId() != null) {
            Location himLocation = GeoSingleton.getInstance().getPersonBearingManager().getPersonLocation();
            if (himLocation != null) {
                him = new PersonOnMap(GeoSingleton.getInstance().getPersonBearingManager().getPersonId(),himLocation.getLatitude(),himLocation.getLongitude());
            }
            //GeoSingleton.getInstance().getPersonBearingManager().
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (him !=null) {
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
        }

        if (me != null) {
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
            Bitmap roundPict = Util.getCroppedBitmap(result);
            LatLng myLatLng = new LatLng(toDraw.getLat(), toDraw.getLon());
            mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title(toDraw.getId())
                    .icon(BitmapDescriptorFactory.fromBitmap(roundPict)));
            if (toDraw == me) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));
            }
        }
    }
}
