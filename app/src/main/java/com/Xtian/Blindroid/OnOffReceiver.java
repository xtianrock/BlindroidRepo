package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Detecta los cambios de estado de la pantalla
 */
public class OnOffReceiver extends BroadcastReceiver {


    private boolean screenOff;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
           screenOff = true;
            screenOnOff(context);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            screenOff = false;
            screenOnOff(context);
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
             /*screenOff = false;
            screenOnOff(context);*/
            //listener.screenOn();
        }
    }

    /**
     * Crea el servicio pasandole como parametro el estado de la pantalla
     * @param context
     */
    private void screenOnOff(Context context) {
        // Crea el servicio y le informa el estado de la pantalla
        Intent i = new Intent(context, BlindroidService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }

}
