package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class OutgoingCallReceiver extends BroadcastReceiver {
    ClaseGlobal vGlobal;

    public OutgoingCallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        vGlobal = (ClaseGlobal) context.getApplicationContext();
        Bundle bundle = intent.getExtras();
        if (bundle == null)
            return;

        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        //Toast.makeText(context, "Llamada entrante: " + number+ " " +  vGlobal.getCountryZipCode(), Toast.LENGTH_SHORT).show();
        vGlobal.findId(number);
    }
}
