package com.Xtian.Blindroid;

/**
 * Interfaz para gestionar el sensor de proximidad
 */
public interface IProximityListener {
    /**
     * El sensor de proximidad esta tapado
     */
    public void near();

    /**
     * El sensor de proximidad no esta tapado
     */
    public void far();
}
