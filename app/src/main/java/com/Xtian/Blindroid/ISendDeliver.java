package com.Xtian.Blindroid;



/**
 * Interfaz para gestionar el envio de SMS
 */
public interface ISendDeliver {


    /**
     * El mensaje se ha entregado correctamente
     */
    public void delivered();

    /**
     * El mensaje se ha entregado
     */
    public void notDelivered();

    /**
     * El mensaje se ha enviado
     */
    public void sent();

    /**
     * El mensaje ha sufrido un fallo generico
     */
    public void genericFailure();

    /**
     * El servicio no esta activo
     */
    public void noService();

    /**
     * null pdu
     */
    public void nullPdu();

    /**
     * La radio esta desactivada
     */
    public void radioOff();
}
