package com.nordman.big.myfellowcompass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.RotateAnimation;

import java.math.BigDecimal;

/**
 * Created by s_vershinin on 31.03.2016.
 *
 */
public class MagnetSensorManager implements SensorEventListener {
    private Context context;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private float mAzimuthDegree = 0f;

    public MagnetSensorManager(Context context) {
        this.context = context;

        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // при изменении сенсора сохранить в глобальной переменной mAzimuthDegree значение азимута в градусах
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            android.hardware.SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            android.hardware.SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            mAzimuthDegree = - round((float) (Math.toDegrees(azimuthInRadians) + 360) % 360, 1);
            //Log.d("LOG", "...AzimuthDegree=" + mAzimuthDegree + "...");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("LOG", "...Sensor Accuracy=" + accuracy + "...");
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void startSensor(){
        mSensorManager.registerListener(this, mAccelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopSensor(){
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    public float getAzimuthDegree(){
        return mAzimuthDegree;
    }

    public float[] getRotateDegrees() {
        float[] result;
        result = new float[2];

        if (Math.abs(mCurrentDegree - mAzimuthDegree)>320) {
            if (Math.abs(mCurrentDegree)>Math.abs(mAzimuthDegree)) {
                // c 360 до 0
                result[0] = mCurrentDegree;
                result[1] = -360;
                mCurrentDegree = 0;
            } else {
                // c 0 на 360
                result[0] = mCurrentDegree;
                result[1] = 0;
                mCurrentDegree = -360;
            }
        } else {
            result[0] = mCurrentDegree;
            result[1] = mAzimuthDegree;
            mCurrentDegree = mAzimuthDegree;
        }

        return result;
    }

}
