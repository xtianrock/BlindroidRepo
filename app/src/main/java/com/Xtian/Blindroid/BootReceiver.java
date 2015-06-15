package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver que inicia el servicio al arrancar el sistema
 */
public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent service = new Intent(context, BlindroidService.class);
            context.startService(service);
        }

    }
}
