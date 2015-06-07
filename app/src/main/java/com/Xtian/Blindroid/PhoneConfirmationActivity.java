package com.Xtian.Blindroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.Xtian.Blindroid.Gcm.GcmUtil;

import java.util.Random;

public class PhoneConfirmationActivity extends Activity implements ISmsListener,ISendDeliver {

    EditText etCountryCode;
    EditText etPhoneNumber;
    Button btSend;
    private static final int MIN = 1000;
    private static final int MAX = 9999;
    private static final String SMS_SENT = "SMS_SENT";
    private static final String SMS_DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPendingIntent;
    PendingIntent deliveredPendingIntent;
    SentReceiver sentReceiver;
    DeliverReceiver deliverReceiver;
    Random random=new Random();
    String code;
    SharedPreferences prefs;
    private GcmUtil gcmUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_confirmation);
        etCountryCode = (EditText)findViewById(R.id.etCode);
        etPhoneNumber = (EditText)findViewById(R.id.etPhone);
        btSend = (Button)findViewById(R.id.btSendSms);
        etCountryCode.setText(Commons.getCountryZipCode(this));
        btSend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
                btSend.setVisibility(View.GONE);
                Toast.makeText(PhoneConfirmationActivity.this,"Mensaje enviado, espere...",Toast.LENGTH_SHORT).show();
            }
        });
        gcmUtil = new GcmUtil(getApplicationContext());

        deliverReceiver=new DeliverReceiver();
        sentReceiver=new SentReceiver();
        sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);
        SentReceiver.startListening(this);
        DeliverReceiver.startListening(this);


    }

    private void sendSms()
    {
        String smsBody=randomCode();
        Log.i("sms","Codigo: "+smsBody);
        String phoneNumber = etCountryCode.getText().toString()+etPhoneNumber.getText().toString();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, smsBody, sentPendingIntent, deliveredPendingIntent);
    }

    private String randomCode() {
        code = Integer.toString(random.nextInt((MAX - MIN) + 1) + MIN);
        return code;
    }

    @Override protected void onResume()
    {
        super.onResume();
        registerReceiver(sentReceiver, new IntentFilter(SMS_SENT));
        registerReceiver(deliverReceiver, new IntentFilter(SMS_DELIVERED));
        SmsReceiver.startListening(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliverReceiver);
        SentReceiver.stopListening();
        DeliverReceiver.stopListening();
        SmsReceiver.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onReceiveSms(String sender, String message) {
        Log.i("sms","Mensaje de: "+sender+", "+message);
        if(message.equals(code))
        {
            Toast.makeText(this,"Telefono confirmado",Toast.LENGTH_SHORT).show();
            Commons.setPhoneNumber(sender);
            String regId=gcmUtil.register(this);
            Log.i("regId","regId : "+regId);
            startService();
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent().setClass(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void startService() {

        Intent i = new Intent(this, BlindroidService.class);
        i.putExtra("screen_state", false);
        startService(i);
    }

    @Override
    public void delivered() {
        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notDelivered() {
        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sent() {
        Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void genericFailure() {
        Toast.makeText(this, "Generic failure cause", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void noService() {
        Toast.makeText(this, "Service is currently unavailable", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void nullPdu() {
        Toast.makeText(this, "No pdu provided", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void radioOff() {
        Toast.makeText(this, "Radio was explicitly turned off", Toast.LENGTH_SHORT).show();
    }
}
