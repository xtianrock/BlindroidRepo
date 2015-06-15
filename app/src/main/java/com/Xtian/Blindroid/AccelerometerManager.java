package com.Xtian.Blindroid;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

/**
 * Clase encargada del control de acelerometro.
 */
public class AccelerometerManager {

    /**
     * Sensibilidad
     */
    public static int THRESHOLD = 30;
    static IAccelerometerListener listener;
    private static Context aContext = null;
    private static long INTERVAL = 900000000;
    /**
     * Listener que captura los eventos de accelerometer listener
     *
     * Se ha eliminado el eje z para que no se active al dejar el telefono sobre una superficie horizontal
     *
     */
    private static SensorEventListener sensorEventListener =
            new SensorEventListener() {

                private long now = 0;
                private long timeDiff = 0;
                private long lastUpdate = 0;
                private long lastShake = 0;

                private float x = 0;
                private float y = 0;
                private float z = 0;
                private float lastX = 0;
                private float lastY = 0;
                private float lastZ = 0;
                private float force = 0;

                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }

                public void onSensorChanged(SensorEvent event) {
                    synchronized (this) {

                        now = event.timestamp;

                        x = event.values[0];
                        y = event.values[1];
                        //z = event.values[2];


                        if (lastUpdate == 0) {
                            lastUpdate = now;
                            lastShake = now;
                            lastX = x;
                            lastY = y;
                            //lastZ = z;


                        } else {
                            timeDiff = now - lastUpdate;

                            if (timeDiff > 0) {

                                //force = Math.abs(x + y + z - lastX - lastY - lastZ);
                                force = Math.abs(x + y - lastX - lastY);

                                if (Float.compare(force, THRESHOLD) > 0) {

                                    if (now - lastShake >= INTERVAL) {

                                        // trigger shake event
                                        listener.onShake(force);
                                    } else if (now - lastShake >= INTERVAL / 5 && now - lastShake < INTERVAL) {
                                        // trigger double shake event
                                        listener.onDoubleShake(force);
                                    }

                                    lastShake = now;
                                }
                                lastX = x;
                                lastY = y;
                                //lastZ = z;
                                lastUpdate = now;
                            }


                        }
                        // trigger change event

                        listener.onAccelerationChanged(x, y, z);
                    }
                }

            };
    private static Sensor sensor;
    private static SensorManager sensorManager;
    /** indica si el phone tiene acelerometro */
    private static Boolean supported;
    /** indica si el acelerometro esta activo */
    private static boolean running = false;

    /**
     * Devuelve true si el aceleromtro esta activo
     */
    public static boolean isListening() {
        return running;
    }

    /**
     * Desactiva el listener
     */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {
            Log.i(Commons.LOGTAG, e.getMessage());
        }
        //Log.i(Commons.LOGTAG, "Acelerometro desactivado");
    }

    /**
     * Recibe un contexto y determina si el servicio es soportado en dicho contexto.
     */
    public static boolean isSupported(Context context) {
        aContext = context;
        if (supported == null) {
            if (aContext != null) {

                sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);

                // guardamos en una lista todos los sensores disponibles del tipo acelerometro
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                supported = Boolean.valueOf(sensors.size() > 0);


            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }

    /**
     * configura el listener para agitar
     *
     * @param threshold
     *            minima variacion de aceleracion para que se considere como agitado
     */
    public static void configure(int threshold) {
        AccelerometerManager.THRESHOLD = threshold;

    }

    /**
     * registra el listener y lo inicia
     * @param IAccelerometerListener
     *              callback para los eventos de acelerometro
     */
    public static void startListening(IAccelerometerListener IAccelerometerListener) {

        sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        if (sensors.size() > 0) {

            sensor = sensors.get(0);
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
            running=true;
            listener = IAccelerometerListener;
            Log.i(Commons.LOGTAG, "Acelerometro activado");

        }


    }

    /**
     * Configura el umbral, registra el listener y lo inicia
     *
     * @param IAccelerometerListener
     *             callback para los eventos de acelerometro
     * @param threshold
     *            minima variacion de aceleracion para que se considere como agitado
     */
    public static void startListening(IAccelerometerListener IAccelerometerListener, int threshold) {
        configure(threshold);
        startListening(IAccelerometerListener);
    }
  
}