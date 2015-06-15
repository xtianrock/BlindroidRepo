package com.Xtian.Blindroid;



/**
 * Interfaz para gestionar el estado del telefono
 */
public interface IPhoneStateListener {

    /**
     * El telefonose encuentra inactivo
     */
    public void onStateIdle();

    /**
     * El telefono esta sonando
     */
    public void onStateRinging();

    /**
     * El telefono s encuentra en una llamada
     */
    public void onStateOffhook();

    /**
     * El telefono esta llamando
     */
    public void onStateCall();
}
