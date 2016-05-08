package com.nordman.big.myfellowcompass;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.blunderer.materialdesignlibrary.fragments.AFragment;

import java.util.Timer;
import java.util.TimerTask;


public class ViewCompassFragment extends AFragment {
    public static final long TICK_INTERVAL = 2000;
    private static final double MIN_SPEED_FOR_ROTATION = 1.2;

    MagnetSensorManager magnetMgr;
    Timer compassTick = null;

    private ImageView imageCompass;
    private ImageView imageArrow;

    public ViewCompassFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LOG","...onCreateView...");
        return inflater.inflate(R.layout.fragment_view_compass, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    }

    @Override
    public void onResume() {
        Log.d("LOG", "...onResume...");
        super.onResume();

        // создаем таймер если еще не создан
        if (compassTick ==null){
            compassTick = new Timer();
            compassTick.schedule(new UpdateCompassTickTask(), 0, TICK_INTERVAL); //тикаем каждую секунду
            Log.d("LOG","...compassTick created...");
        }
        magnetMgr.startSensor();
    }

    @Override
    public void onPause() {
        Log.d("LOG", "...onPause...");
        super.onPause();

        if (compassTick !=null) {
            compassTick.cancel();
            compassTick = null;
        }
        magnetMgr.stopSensor();
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
            Log.d("LOG","...curSpeed = " + curSpeed);
            if (curSpeed < MIN_SPEED_FOR_ROTATION) {
                Log.d("LOG","...magnet rotation...");

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
            imageArrow.startAnimation(ra);

            float distanceToPerson = GeoSingleton.getInstance().getPersonBearingManager().getDistance();
            float azimuthToPerson = GeoSingleton.getInstance().getPersonBearingManager().getAzimuthDegree();
            Log.d("LOG","...distanceToPerson = " + distanceToPerson);
            Log.d("LOG","...azimuthToPerson = " + azimuthToPerson);
            //((TextView)findViewById(R.id.textExtra1)).setText("Dist = " + String.valueOf(distanceToPerson)+" meters. Azimuth = " + String.valueOf(azimuthToPerson) + "°");

            return false;
        }
    });

}
