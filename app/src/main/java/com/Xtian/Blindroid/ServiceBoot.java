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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ServiceBoot extends Service implements AccelerometerListener {


    static ArrayList<Contacto> contactos = new ArrayList<Contacto>();
    boolean screenOff;
    ClaseGlobal vGlobal;
    SharedPreferences prefs;
    BroadcastReceiver mReceiver;
    TelephonyManager telephonyManager;
    PhoneStateListener callStateListener;
    private NotificationManagerCompat notificationManagerCompat;
    //Vacia los contactos y los vuelve a cargar cuando s eproduce un cambio en la bd contactos.
    private ContentObserver mObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            contactos.clear();
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

        this.getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);

        // instancio el receiver que controla el estado de la pantalla.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new OnOffReceiver();
        registerReceiver(mReceiver, filter);
        Log.i("xtian", "Servicio creado y receiver registrado");


        prefs = PreferenceManager.getDefaultSharedPreferences(this);


        //instancio el listener que detecta si se esta en una llamada o no
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        callStateListener = new PhoneStateListener() {
            public void onCallStateChanged(int state, String incomingNumber) {

                vGlobal = (ClaseGlobal) getApplicationContext();

                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    activarAcel();
                    //Cambia a false la variable global que permite saber si la llamada ha sido realizada con la aplicacion
                    vGlobal.setLlamando(false);
                    vGlobal.inCall = false;
                    if (vGlobal.getNotificationActive()) {
                        vGlobal.setNotificationActive(false);
                        notificationManagerCompat = NotificationManagerCompat.from(ServiceBoot.this);
                        notificationManagerCompat.cancel(002);
                    }

                } else if (state == TelephonyManager.CALL_STATE_RINGING) {
                    comprobarYpararAcel();
                    //Toast.makeText(getBaseContext(), "Llamada entrante: "+incomingNumber+ " " +  vGlobal.getCountryZipCode(),Toast.LENGTH_SHORT).show();
                    vGlobal.setLlamando(false);
                    vGlobal.findId(incomingNumber);
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //comprobarYpararAcel();
                    Log.i("xtian", Boolean.toString(vGlobal.getLlamando()));
                    if (vGlobal.getLlamando()) {
                        llamadaBlindroid();
                    } else {
                        setSpeaker(false);
                    }
                    vGlobal.inCall = true;
                    final String numero = incomingNumber;
                    if (prefs.getBoolean("wear", false)) {
                        vGlobal.setNotificationActive(true);
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                Notification();
                            }
                        };

                        Timer timer = new Timer();
                        timer.schedule(task, 50);
                    }


                }
            }


        };
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);


        Toast.makeText(getBaseContext(), R.string.activado,
                Toast.LENGTH_SHORT).show();

        getNameNumber();


    }

    private void Notification() {


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
                new NotificationCompat.Action.Builder(R.drawable.ic_action_device_access_end_call,
                        "Colgar", pendingIntentColgar)
                        .build();

        NotificationCompat.Action actionSpeaker =
                new NotificationCompat.Action.Builder(R.drawable.ic_action_device_access_volume_on,
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

    private void llamadaBlindroid() {
        if (prefs.getBoolean("speaker", false)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            setSpeaker(true);
        } else {
            setSpeaker(false);
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Creo la notificacion

        Intent ajustes = new Intent(ServiceBoot.this, MainActivity.class);
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


        try {
            screenOff = intent.getBooleanExtra("screen_state", false);

        } catch (Exception e) {
        }
        if (!screenOff) {
            activarAcel();
        } else {
            comprobarYpararAcel();
        }
        Log.i("xtian", "onStartCommand del servicio, screenOff:" + screenOff);

        // con sticky mantengo el servicio hasta que se le ordene parar
        return START_STICKY;
    }

    private void activarAcel() {

        comprobarYpararAcel();
        String sensibilidad = prefs.getString("sensibilidad", "29");
        if (AccelerometerManager.isSupported(this)) {

            //Start Accelerometer Listening
            AccelerometerManager.startListening(this, Integer.parseInt(sensibilidad));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Si el sensor esta activo lo desactivo
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
            Toast.makeText(getBaseContext(), R.string.desactivado, Toast.LENGTH_SHORT).show();
        }
        unregisterReceiver(mReceiver);
        contactos.clear();
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
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
            //Aqu� relleno los dos
            ClaseGlobal vGlobal = new ClaseGlobal();
            //Reemplazo los caracteres acentuados por los normales
            String nombre = vGlobal.reemplazarCaracteresRaros(names.getString(indexName)).toLowerCase();
            String nombreOriginal = names.getString(indexName);
            String codPais = vGlobal.getCountryZipCode(this);
            String numero = vGlobal.prepararNumero(names.getString(indexNumber), codPais);
            numero = codPais + numero;
            // Toast.makeText(getBaseContext(),numero,Toast.LENGTH_SHORT).show();
            String id = names.getString(indexID);
            Contacto cont = new Contacto(nombre, nombreOriginal, numero, id);
            contactos.add(cont);
        } while (names.moveToNext());
        names.close();
    }


    private void comprobarYpararAcel() {
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    @Override
    public void onAccelerationChanged(float x, float y, float z) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onDubleShake(float force) {
        if (vGlobal.inCall) {
            if (prefs.getBoolean("colgar", false)) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(200);
                final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
                try {
                    vGlobal.colgar();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onShake(float force) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!vGlobal.inCall) {
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


}

