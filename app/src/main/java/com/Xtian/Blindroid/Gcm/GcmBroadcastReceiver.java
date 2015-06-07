package com.Xtian.Blindroid.Gcm;
/**
 * Created by xtianrock on 01/05/2015.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.Xtian.Blindroid.Commons;
import com.Xtian.Blindroid.NotificationIntentService;
import com.google.android.gms.gcm.GoogleCloudMessaging;


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);
        Intent intentService = new Intent(context, NotificationIntentService.class);
        Bundle extras = intent.getExtras();

        intentService.putExtra(Commons.MSG, extras.getString(Commons.MSG));
        intentService.putExtra(Commons.FROM, extras.getString(Commons.FROM));
        intentService.putExtra(Commons.TO, extras.getString(Commons.TO));
        intentService.putExtra("gcmType",messageType);


        startWakefulService(context, intentService);
        setResultCode(Activity.RESULT_OK);
    }
}