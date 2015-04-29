package com.Xtian.Blindroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by xtianrock on 17/04/2015.
 */
public class PhoneStateReceiver extends BroadcastReceiver {

    public static String TAG="PhoneStateReceiver";
    static IPhoneStateListener listener;
    private ClaseGlobal vGlobal;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "PhoneStateReceiver**Call State=" + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(TAG,"PhoneStateReceiver**Idle");
                listener.onStateIdle();
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Incoming call
                String incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG,"PhoneStateReceiver**Incoming call " + incomingNumber);
                listener.onStateRinging();

              /*  if (!killCall(context)) { // Using the method defined earlier
                    Log.d(TAG,"PhoneStateReceiver **Unable to kill incoming call");
                }*/

            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(TAG,"PhoneStateReceiver **Offhook");
                listener.onStateOffhook();
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG,"PhoneStateReceiver **Outgoing call " + outgoingNumber);
            listener.onStateCall();
            vGlobal = (ClaseGlobal) context.getApplicationContext();
            vGlobal.findId(outgoingNumber);
           // setResultData(null); // Kills the outgoing call

        } else {
            Log.d(TAG,"PhoneStateReceiver **unexpected intent.action=" + intent.getAction());
        }
    }


    public static void startListening(IPhoneStateListener IPhoneListener) {
        listener = IPhoneListener;
    }
}