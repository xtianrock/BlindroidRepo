package com.Xtian.Blindroid;

/**
 * Interfaz para gestionar el acelerometro
 */
public interface IAccelerometerListener {

    /**
     * Determina un cambio de aceleracion
     * @param x
     * @param y
     * @param z
     */
    public void onAccelerationChanged(float x, float y, float z);

    /**
     * Determina una agitacion
     * @param force
     */
    public void onShake(float force);


    /**
     * Determina una agitacion doble
     * @param force
     */
    public void onDoubleShake(float force);

}