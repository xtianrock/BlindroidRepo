package com.Xtian.Blindroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by xtianrock on 12/05/2015.
 */

/**
 * Receiver para los cambios de estado en la entrega de mensaje
 */
class DeliverReceiver extends BroadcastReceiver {

    private static ISendDeliver listener;
    @Override
    public void onReceive(Context context, Intent arg1) {

        if(listener!=null) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    listener.delivered();
                    break;
                case Activity.RESULT_CANCELED:
                    listener.notDelivered();
                    break;
            }
        }
    }

    /**
     * Comienza la escucha
     * @param iSendDeliver
     */
    public static void startListening(ISendDeliver iSendDeliver)
    {
        listener=iSendDeliver;
    }

    /**
     * Detiene la escucha
     */
    public static void stopListening()
    {
        listener=null;
    }
}