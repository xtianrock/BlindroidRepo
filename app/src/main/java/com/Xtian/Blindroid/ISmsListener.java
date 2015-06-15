package com.Xtian.Blindroid;

/**
 * Interfaz para gestionar el acelerometro
 */
public interface ISmsListener {

  /**
   * Mensaje recibido desde el sender
   * @param sender
   * @param message
   */
  public void onReceiveSms(String sender, String message);
}
