package com.Xtian.Blindroid;

import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.telephony.TelephonyManager;

import com.Xtian.Blindroid.Gcm.Constants;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Clase global
 */
public class Commons extends Application {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat[] df = new DateFormat[] {
            DateFormat.getDateInstance(), DateFormat.getTimeInstance()};
    boolean notificationActive = false;
    public String callPhone;
    public static final String LOGTAG = "blindroid-log";
    public static final String PROFILE_ID = "profile_id";

    //parametros reconocidos por el servidor
    public static final String FROM = "chatId";
    public static final String REG_ID = "regId";
    public static final String MSG = "msg";
    public static final String TO = "chatId2";

    private static SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Devuelve true o false dependiendo de si
     * la notificacion de llamada en curso esta activa
     * @return notificationActive
     */
    public boolean getNotificationActive() {
        return notificationActive;
    }

    /**
     * Asigna un valor booleano ala variable
     * @param notificationActive
     */
    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

    /**
     * Devuelve el telefono de la persona
     * con la que se mantiene la llamada
     * @return callPhone
     */
    public String getCallPhone() {
        return callPhone;
    }

    /**
     * Establece el telefono de la persona
     * con la que mantiene una llamada
     * @param callPhone
     */
    public void setCallPhone(String callPhone) {
        this.callPhone = callPhone;
    }

    /**
     * Devuelve el telefono del usuario
     * @return phone
     */
    public static String getPhoneNumber() {
        return prefs.getString("phone", "");
    }

    /**
     * Establece el telefono del usuario
     * @param phone
     */
    public static void setPhoneNumber(String phone) {
        prefs.edit().putString("phone", phone).apply();
    }

    /**
     * Obtiene el id del usuario con el que se mantiene
     * la conversacion
     * @return
     */
    public static String getCurrentChat() {
        return prefs.getString("current_chat", "");
    }

    /**
     * Establece el id del contacto con el cual
     * se mantiene la conversacion
     * @param chatId
     */
    public static void setCurrentChat(String chatId) {
        prefs.edit().putString("current_chat", chatId).apply();
    }


    /**
     * Obyiene el tono de notificacion predeterminado del sistema
     * @return ringtone
     */
    public static String getRingtone() {
        return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    /**
     * Devuelve la url del servidor
     * @return server url
     */
    public static String getServerUrl() {
        return Constants.SERVER_URL;
    }

    /**
     * Obtiene el api_key de GCM
     * @return
     */
    public static String getApiKey() { return  Constants.Api_key;
    }


    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    /**
     * Obtiene el tracker de google analitycs
     * @param trackerId
     * @return synchronized
     */
    public synchronized Tracker getTracker(TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker(R.xml.app_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public enum TrackerName {
        APP_TRACKER,
        GLOBAL_TRACKER,
        E_COMMERCE_TRACKER,
    }

    /**
     * Obtiene el id del telefono dado
     * @param number
     * @return id
     */
   public static long getID(String number) {
        for (Contact contact:BlindroidService.contacts) {
            if (contact.getPhone().equals(number)) {
                return contact.getID();
            }
        }
        return -1;
    }

    /**
     * Obtiene el Stream con la foto del contacto
     * @param context
     * @param phone
     * @return inpuStream
     */
    public static InputStream openPhoto(Context context,String phone) {
        if(!phone.contains("+"))
            phone="+"+phone;
        long contactID=getID(phone);
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Obtiene la foto en formato redondo del contacto con el telefono dado.
     * @param context
     * @param phone
     * @return
     */
    public static RoundedBitmapDrawable getContactPhoto(Context context, String phone) {
        InputStream inputStream= openPhoto(context, phone);
        Bitmap bitmap;
        if(inputStream==null)
        {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.blindroid_icon);
        }
        else
        {
            bitmap = BitmapFactory.decodeStream(inputStream);
        }

        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        dr.setCornerRadius(Math.min(dr.getMinimumWidth(),dr.getMinimumHeight()));
        return dr;

    }


    /**
     * Remplaza caracteres acentuados por normales.
     * @param input
     * @return
     */
    public static String replaceAcentedCharacters(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuÁÀÄÉÈËÍÌÏÓÒÖÚÙÜ";
        // Cadena de caracteres ASCII que reemplazar�n los originales.
        String ascii = "aaaeeeiiiooouuuAAAEEEIIIOOOUUU";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }
        return output;
    }

    /**
     * Comprueba la existencia de un paquete
     * @param paquete
     * @return
     */
    public boolean existePaquete(String paquete) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        final List<ResolveInfo> pkgAppsList = getPackageManager().queryIntentActivities(mainIntent, 0);
        boolean packageExists = false;
        boolean activityExists = false;
        for (PackageInfo r : packs) {
            String pkg = r.applicationInfo.packageName;
            if (pkg != null && pkg.equals(paquete)) {
                packageExists = true;
                break;
            }
        }
        if (!packageExists)
            return false;

        for (ResolveInfo r : pkgAppsList) {
            ActivityInfo info = r.activityInfo;
            if (info != null && info.name != null && info.name.equals("com.google.android.googlequicksearchbox.VoiceSearchActivity")) {
                activityExists = true;
                break;
            }
        }
        return activityExists;
    }

    /**
     * Detiene una llamada
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void endCall() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TelephonyManager tm = (TelephonyManager) getApplicationContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        Class c = null;
        try {
            c = Class.forName(tm.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method m = c.getDeclaredMethod("getITelephony");
        m.setAccessible(true);
        Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
        try {
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
        m.setAccessible(true); // Make it accessible
        m.invoke(telephonyService); // invoke endCall()

    }

    /**
     * Obtiene el codigo de pais de un telefono dado
     * @param context
     * @return zipCode
     */
    public static String getCountryZipCode(Context context) {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    /**
     * Obtiene un contacto por su telefono
     * @param phone
     * @return contact
     */
    public static Contact getContactByPhone(String phone)
    {
        Contact contact=null;
        for (Contact item:BlindroidService.contacts) {
            if (item.getPhone().contains(phone)) {
                contact=item;
            }
        }
        return contact;
    }

    /**
     * Obtiene los contactos que coinciden con el criterio de busqueda dado
     * @param results
     * @return arraylist de contactos
     */
    public static ArrayList<Contact> getMatchingContacts(ArrayList<String> results)
    {
        ArrayList<Contact> contacts= new ArrayList<>();

        for (String result:results)
        {
            String name = Commons.replaceAcentedCharacters(result).toLowerCase();
            for (Contact contact:BlindroidService.contacts)
            {
                if(contact.getName().contains(name))
                {
                    if(contact.getName().equals(name))
                    {
                        contacts.clear();
                        contacts.add(contact);
                        return contacts;
                    }
                    if(!contacts.contains(contact))
                        contacts.add(contact);
                }
            }
        }

        return contacts;
    }

    /**
     * Da formato a la fecha dada
     * @param datetime
     * @return datetime formateado
     */
    public static String getDisplayTime(String datetime) {
        try {
            Date now=new Date();
            Date dt = sdf.parse(datetime);
            if (now.getYear()==dt.getYear() && now.getMonth()==dt.getMonth() && now.getDate()==dt.getDate()) {
                return df[1].format(dt);
            }
            return df[0].format(dt);
        } catch (ParseException e) {
            return datetime;
        }
    }

}
