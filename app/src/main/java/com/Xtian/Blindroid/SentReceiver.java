package com.Xtian.Blindroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

/**
 * Created by xtianrock on 12/05/2015.
 */
class SentReceiver extends BroadcastReceiver {

    private static ISendDeliver listener;
    @Override
    public void onReceive(Context context, Intent arg1) {

        if(listener!=null)
        {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    listener.sent();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    listener.genericFailure();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    listener.noService();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    listener.nullPdu();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    listener.radioOff();
                    break;
            }
        }
    }
    public static void startListening(ISendDeliver iSendDeliver)
    {
        listener=iSendDeliver;
    }
    public static void stopListening()
    {
        listener=null;
    }

}