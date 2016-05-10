package com.nordman.big.myfellowcompass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.blunderer.materialdesignlibrary.fragments.AFragment;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;


public class ViewCompassFragment extends AFragment {
    public static final long TICK_INTERVAL = 2000;
    private static final double MIN_SPEED_FOR_ROTATION = 1.2;

    MagnetSensorManager magnetMgr;
    Timer compassTick = null;

    private ImageView personSelector = null;
    private ImageView imageCompass;
    private ImageView imageArrow;

    public ViewCompassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Log.d("LOG","...onCreateView...");
        return inflater.inflate(R.layout.fragment_view_compass, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personSelector = (ImageView) getActivity().findViewById(R.id.mapPersonSelector);
        personSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("LOG","...select person stub...");
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

        imageCompass = (ImageView) getActivity().findViewById(R.id.imageViewCompass);
        imageArrow = (ImageView) getActivity().findViewById(R.id.imageViewArrow);

        if (magnetMgr == null) {
            magnetMgr = new MagnetSensorManager(getContext());
        }

        ImageView imageMap = (ImageView)getActivity().findViewById(R.id.compassMap);
        imageMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((NavigationDrawerActivity) getActivity()).performNavigationDrawerItemClick(0);
            }
        });

        final PersonOnMap him = GeoSingleton.getInstance().getHimOnMap();
        if (him != null) {
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

            setPersonInfo();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("LOG","... ViewCompassFragment.onActivityResult  requestCode = " + requestCode);

        // check on SelectPerson result
        if (requestCode==1) {
            if (data != null)

            GeoSingleton.getInstance().getPersonBearingManager().setPersonId(data.getStringExtra("id"));
            GeoSingleton.getInstance().getPersonBearingManager().setPersonName(data.getStringExtra("name"));
            GeoSingleton.getInstance().getGeoEndpointManager().getGeo(GeoSingleton.getInstance().getPersonBearingManager().getPersonId());

            if (GeoSingleton.getInstance().getHimOnMap() == null) {
                final PersonOnMap him = new PersonOnMap(data.getStringExtra("id"),data.getStringExtra("name"));
                GeoSingleton.getInstance().setHimOnMap(him);
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

            GeoSingleton.getInstance().getGeoGPSManager().setMode(GeoGPSManager.ACTIVE_MODE);

            //Log.d("LOG","...person id = " + data.getStringExtra("id"));
            //Log.d("LOG","...me location = " + GeoSingleton.getInstance().getGeoGPSManager().getCurrentLocation().toString());

            setPersonInfo();
        }
    }


    public void setPersonInfo(){
        if (GeoSingleton.getInstance().getHimOnMap()!=null) {
            getActivity().findViewById(R.id.layoutDistance).setVisibility(View.VISIBLE);
            imageArrow.setVisibility(View.VISIBLE);

            float distanceValue = GeoSingleton.getInstance().getPersonBearingManager().getDistance()/1000;
            if (distanceValue > 0) {
                TextView textDistance = (TextView) getActivity().findViewById(R.id.textDistance);
                NumberFormat f = NumberFormat.getInstance(getResources().getConfiguration().locale);
                f.setMaximumFractionDigits(2);
                textDistance.setText(getString(R.string.distance_value, f.format(distanceValue)));
            }
        }
    }


    @Override
    public void onResume() {
        //Log.d("LOG", "...onResume...");
        super.onResume();

        // создаем таймер если еще не создан
        if (compassTick ==null){
            compassTick = new Timer();
            compassTick.schedule(new UpdateCompassTickTask(), 0, TICK_INTERVAL); //тикаем каждую секунду
            //Log.d("LOG","...compassTick created...");
        }
        magnetMgr.startSensor();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onPause() {
        //Log.d("LOG", "...onPause...");
        super.onPause();

        if (compassTick !=null) {
            compassTick.cancel();
            compassTick = null;
        }
        magnetMgr.stopSensor();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private class UpdateCompassTickTask extends TimerTask {
        public void run() {
            compassTickHandler.sendEmptyMessage(0);
        }
    }

    final Handler compassTickHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            double curSpeed = GeoSingleton.getInstance().getGeoGPSManager().getSpeed();
            String provider = GeoSingleton.getInstance().getGeoGPSManager().getGPSProvider();

            Log.d("LOG","...provider = " + GeoSingleton.getInstance().getGeoGPSManager().getGPSProvider());


            if ((curSpeed < MIN_SPEED_FOR_ROTATION) || (provider.equals("network"))) {
                ((TextView) getActivity().findViewById(R.id.textExtra)).setText(R.string.magnet_rotation);

                // compass image
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

                // arrow image
                imageArrow.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.arrow_light));
                if (GeoSingleton.getInstance().getHimOnMap()!=null) {
                    if (provider.equals("network")){
                        ((TextView) getActivity().findViewById(R.id.textGPSModeRequired)).setText(R.string.gps_mode_required);
                    } else {
                        ((TextView) getActivity().findViewById(R.id.textGPSModeRequired)).setText(R.string.keep_on_moving);
                    }
                }

                setPersonInfo();

                return false;
            }


            ((TextView) getActivity().findViewById(R.id.textGPSModeRequired)).setText("");
            imageArrow.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.arrow));
            getActivity().findViewById(R.id.textGPSModeRequired).setVisibility(View.INVISIBLE);

            ((TextView) getActivity().findViewById(R.id.textExtra)).setText("...gps rotation...");
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
            imageArrow.startAnimation(ra);

            setPersonInfo();

            return false;
        }
    });

    private class GetProfileImageTask extends AsyncTask<PersonOnMap, Integer, Bitmap> {
        // Do the long-running work in here
        protected Bitmap doInBackground(PersonOnMap... person) {
            Bitmap result = null;
            URL imgUrl;
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
            Bitmap roundPict = Util.getCroppedBitmap(result);
            personSelector.setImageBitmap(roundPict);
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
        //Log.d("LOG","...onGPSLocationChanged...");
    }

}
