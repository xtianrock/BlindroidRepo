package com.Xtian.Blindroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;


public class BlindroidService extends Service implements IAccelerometerListener, IPhoneStateListener, IProximityListener {


    private boolean screenOff;
    public enum phoneStates{IDLE,RINGING,OFFHOOK,CALL};
    public phoneStates state = phoneStates.IDLE;
    static ArrayList<Contact> contacts = new ArrayList<>();
    ClaseGlobal vGlobal;
    SharedPreferences prefs;
    BroadcastReceiver screenReceiver;
    private NotificationManagerCompat notificationManagerCompat;

    //Vacia los contactos y los vuelve a cargar cuando se produce un cambio en la bd contactos.
    private ContentObserver contactsObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            contacts.clear();
            getNameNumber();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Registro el ContentObserver que estará pendiente de cambios en la base de datos de contactos.
        this.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsObserver);

        // instancio el receiver que controla el estado de la pantalla.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        screenReceiver = new OnOffReceiver();
        registerReceiver(screenReceiver, filter);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        vGlobal = (ClaseGlobal) getApplicationContext();

        Toast.makeText(getBaseContext(), R.string.activado,Toast.LENGTH_SHORT).show();
        getNameNumber();

        Log.i("xtian", "onCreate");

    }

    private void wearNotification() {


        NotificationManagerCompat notificationManagerCompat;

        Intent intentSpeaker = new Intent(this, ActivityColgar.class);
        intentSpeaker.putExtra("metodo", 1);
        intentSpeaker.setAction("altavoz");

        PendingIntent pendingIntentSpeaker = PendingIntent.getActivity(this, 0, intentSpeaker, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentColgar = new Intent(this, ActivityColgar.class);
        intentColgar.putExtra("metodo", 0);
        intentSpeaker.setAction("colgar");

        PendingIntent pendingIntentColgar = PendingIntent.getActivity(this, 0, intentColgar, PendingIntent.FLAG_UPDATE_CURRENT);

// Create the action
        NotificationCompat.Action actionColgar =
                new NotificationCompat.Action.Builder(R.drawable.blindroid_icon,
                        "Colgar", pendingIntentColgar)
                        .build();

        NotificationCompat.Action actionSpeaker =
                new NotificationCompat.Action.Builder(R.drawable.blindroid_icon,
                        "Altavoz", pendingIntentSpeaker)
                        .build();

        // the main notification that launches the greeting UI on the handheld
        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(vGlobal.getNombreLlamada())
                .setContentTitle("Llamada en curso")
                .setSmallIcon(R.drawable.blindroid_logo)
                .setAutoCancel(true)
                .extend(new NotificationCompat.WearableExtender()
                        .addAction(actionColgar)
                        .addAction(actionSpeaker)
                        .setBackground(BitmapFactory.decodeStream(openPhoto(vGlobal.getIdLlamada()))))
                .build();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(002, notification);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Creo la notificacion

        Intent ajustes = new Intent(BlindroidService.this, MainActivity.class);
        PendingIntent touch = PendingIntent.getActivity(this, 0, ajustes, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder bBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.blindroid_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.abre_ajustes))
                                //.setAutoCancel(true)
                        .setOngoing(true)
                        .setContentIntent(touch)
                        .setPriority(Notification.PRIORITY_MIN);
        Notification barNotif = bBuilder.build();
        startForeground(1, barNotif);

        PhoneStateReceiver.startListening(this);

        try {
            screenOff = intent.getBooleanExtra("screen_state", false);

        } catch (Exception e) {
        }
        if (!screenOff) {
            String sensibilidad = prefs.getString("sensibilidad", "29");
            if (AccelerometerManager.isSupported(this)) {
                AccelerometerManager.startListening(this, Integer.parseInt(sensibilidad));
            }
        } else {
            if (AccelerometerManager.isListening()) {
                AccelerometerManager.stopListening();
            }
        }
        Log.i("xtian", "onStartCommand del servicio, screenOff:" + screenOff);


        // con sticky mantengo el servicio hasta que se le ordene parar
        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        //Desactivo sensores y listeners
        if (ProximityManager.isListening()) {
            ProximityManager.stopListening();
        }
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
            Toast.makeText(getBaseContext(), R.string.desactivado, Toast.LENGTH_SHORT).show();
        }
        unregisterReceiver(screenReceiver);
        this.getContentResolver().unregisterContentObserver(contactsObserver);
        contacts.clear();
        Log.i("xtian", "service onDestroy");
    }


    private void getNameNumber() {

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor names = getContentResolver().query(
                uri, projection, null, null, null);
        int indexName = names.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexID = names.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        int indexNumber = names.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.NUMBER);
        names.moveToFirst();
        do {
            //Aqui relleno los dos
            ClaseGlobal vGlobal = new ClaseGlobal();
            //Reemplazo los caracteres acentuados por los normales
            String nombre = vGlobal.reemplazarCaracteresRaros(names.getString(indexName)).toLowerCase();
            String nombreOriginal = names.getString(indexName);
            String codPais = vGlobal.getCountryZipCode(this);
            String numero = vGlobal.prepararNumero(names.getString(indexNumber), codPais);
            numero = codPais + numero;
            // Toast.makeText(getBaseContext(),numero,Toast.LENGTH_SHORT).show();
            String id = names.getString(indexID);
            Contact cont = new Contact(nombre, nombreOriginal, numero, id);
            contacts.add(cont);
        } while (names.moveToNext());
        names.close();
    }



    @Override
    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onDoubleShake(float force) {
        if (state==phoneStates.OFFHOOK) {
            if (prefs.getBoolean("colgar", false)) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
                try {
                    vGlobal.colgar();
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onShake(float force) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (state==phoneStates.IDLE) {
            v.vibrate(200);
            Intent i = new Intent(this, ReconocimientoVoz.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            getApplication().startActivity(i);
            Log.i("xtian", "OnShake.");
        }


    }

    private void setSpeaker(boolean speaker) {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioManager.setSpeakerphoneOn(speaker);
    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    public void onStateIdle() {
        state=phoneStates.IDLE;
        if (ProximityManager.isListening())
            ProximityManager.stopListening();
        if (vGlobal.getNotificationActive()) {
            vGlobal.setNotificationActive(false);
            notificationManagerCompat = NotificationManagerCompat.from(BlindroidService.this);
            notificationManagerCompat.cancel(002);
        }

        Log.i("xtian", "Estado idle");
    }

    @Override
    public void onStateRinging() {
        state=phoneStates.RINGING;
        Log.i("xtian", "Estado ringing");
    }

    @Override
    public void onStateOffhook() {
        state=phoneStates.OFFHOOK;
        if(ProximityManager.isSupported(this))
        ProximityManager.startListening(this);
        vGlobal.setNotificationActive(true);
        wearNotification();
        Log.i("xtian", "Estado offHook");
    }

    @Override
    public void onStateCall() {
        state=phoneStates.CALL;
        Log.i("xtian", "Estado calling");
    }

    @Override
    public void near() {
        if (prefs.getBoolean("speaker", true))
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setSpeaker(false);
        }
        Log.i("xtian", "proximity near");
    }

    @Override
    public void far() {
        if (prefs.getBoolean("speaker", true))
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setSpeaker(true);
        }
        Log.i("xtian", "proximity far");
    }

}
