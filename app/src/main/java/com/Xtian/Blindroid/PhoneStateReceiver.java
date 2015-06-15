package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Esta pendiente de los cambios de estado del servicio de telefonia
 */
public class PhoneStateReceiver extends BroadcastReceiver {

    public static String TAG="PhoneStateReceiver";
    static IPhoneStateListener listener;
    private Commons commons;

    @Override
    public void onReceive(Context context, Intent intent) {
        commons = (Commons) context.getApplicationContext();
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            //Log.d(TAG, "PhoneStateReceiver**Call State=" + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //Log.d(TAG,"PhoneStateReceiver**Idle");
                if(listener!=null)
                listener.onStateIdle();
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Incoming call
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if(!incomingNumber.matches("^\\+(?:[0-9] ?){6,14}[0-9]$"))
                {
                    incomingNumber = Commons.getCountryZipCode(context) + incomingNumber;
                }
                commons.setCallPhone(incomingNumber);
                Log.d(TAG,"PhoneStateReceiver**Incoming call " + incomingNumber);
                if(listener!=null)
                listener.onStateRinging();
            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                //Log.d(TAG,"PhoneStateReceiver **Offhook");
                if(listener!=null)
                listener.onStateOffhook();
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "PhoneStateReceiver **Outgoing call " + outgoingNumber);
            commons.setCallPhone(outgoingNumber);
            if(listener!=null)
                listener.onStateCall();
        }
    }

    /**
     * Inicia la escucha del estado del servicio de telefonia
     * @param IPhoneListener
     */
    public static void startListening(IPhoneStateListener IPhoneListener) {
        listener = IPhoneListener;
    }
}