package com.nordman.big.myfellowcompass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PersonOnMap me = null;

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
        Log.d("LOG", "...My id = " + me.getId());
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
        /*
            mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title("Me")
                    .icon(BitmapDescriptorFactory.fromBitmap(profPict)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
        }
        */
    }

    private class GetProfileImageTask extends AsyncTask<PersonOnMap, Integer, Bitmap> {
        // Do the long-running work in here
        protected Bitmap doInBackground(PersonOnMap... person) {
            Bitmap result = null;
            URL imgUrl = null;
            try {
                imgUrl = new URL("https://graph.facebook.com/" + person[0].getId() + "/picture?type=normal" );

                InputStream in = (InputStream) imgUrl.getContent();
                result = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Bitmap result) {
            Bitmap roundPict = getCroppedBitmap(result);
            LatLng myLatLng = new LatLng(me.getLat(), me.getLon());
            mMap.addMarker(new MarkerOptions()
                    .position(myLatLng)
                    .title("Me")
                    .icon(BitmapDescriptorFactory.fromBitmap(roundPict)));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12.0f));

        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(bitmap, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        p.setStrokeWidth(3);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        return output;
    }
}
