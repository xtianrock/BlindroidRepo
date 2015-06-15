package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //boolean inicio = prefs.getBoolean("inicio", false);
        // LANZAR SERVICIO
       // if (inicio) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent service = new Intent(context, BlindroidService.class);
                context.startService(service);
            }

    }


}
