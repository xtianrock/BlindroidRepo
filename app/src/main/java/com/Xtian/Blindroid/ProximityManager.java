package com.Xtian.Blindroid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import java.util.List;

/**
 * Created by xtianrock on 17/04/2015.
 */
public class ProximityManager {
    private static IProximityListener listener;
    private static SensorManager sensorManager;
    private static Sensor proximity;
    private static Context pContext= null;
    private static Boolean supported;
    private static boolean running;

    private static SensorEventListener sensorEventListener =
            new SensorEventListener() {


                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

                public void onSensorChanged(SensorEvent event) {

                    if (event.values[0] == 0) {
                       listener.near();
                    } else {
                       listener.far();
                    }
                }
            };


    public static boolean isSupported(Context context) {
        pContext = context;
        if (supported == null) {
            if (pContext != null) {
                sensorManager = (SensorManager) pContext.getSystemService(Context.SENSOR_SERVICE);
                // guardamos en una lista todos los sensores disponibles del tipo acelerometro
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_PROXIMITY);
                supported = Boolean.valueOf(sensors.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }

    public static boolean isListening() {
        return running;
    }

    public static void startListening(IProximityListener IProximityListener) {
        listener=IProximityListener;
        sensorManager = (SensorManager) pContext.getSystemService(Context.SENSOR_SERVICE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(sensorEventListener, proximity, SensorManager.SENSOR_DELAY_GAME);
        running=true;
    }
    public static void stopListening() {
        sensorManager.unregisterListener(sensorEventListener);
        running=false;
    }
}
