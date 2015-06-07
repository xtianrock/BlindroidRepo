package com.Xtian.Blindroid;

/**
 * Created by xtianrock on 11/05/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    static ISmsListener listener;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("sms","onReceive");
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;

        if (bundle != null) {
            // Retrieve the SMS Messages received
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            String sender="";
            String message="";
            // For every SMS message received
            for (int i=0; i < msgs.length; i++) {
                // Convert Object array
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                // Sender's phone number
                sender=msgs[i].getOriginatingAddress().replace("+","").trim();
                // Fetch the text message
                message= msgs[i].getMessageBody();
                Log.d("sms", sender+" "+message);
                if(listener!=null)
                listener.onReceiveSms(sender, message);
            }
        }
    }

    public static void startListening(ISmsListener iSmsListener)
    {
        listener=iSmsListener;
    }
    public static void stopListening()
    {
        listener=null;
    }
}