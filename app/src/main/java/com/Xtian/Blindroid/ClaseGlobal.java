package com.Xtian.Blindroid;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ClaseGlobal extends Application {

    boolean appLlamando = false;
    boolean screenOff = false;
    boolean busquedaInstalada = false;
    boolean notificationActive = false;
    boolean inCall;
    long idLlamada;
    String nombreLlamada;

    public boolean getLlamando() {
        return appLlamando;
    }

    public void setLlamando(boolean llamando) {
        appLlamando = llamando;
    }

    public boolean getScreenOff() {
        return screenOff;
    }

    public void setScreenOff(boolean screenOff) {
        this.screenOff = screenOff;
    }

    public boolean getBusquedaInstalada() {
        return busquedaInstalada;
    }

    public void setBusquedaInstalada(boolean instalada) {
        busquedaInstalada = instalada;
    }

    public boolean getNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

    public long getIdLlamada() {
        return idLlamada;
    }

    public void setIdLlamada(long idLlamada) {
        this.idLlamada = idLlamada;
    }

    public String getNombreLlamada() {
        return nombreLlamada;
    }

    public void setNombreLlamada(String nombreLlamada) {
        this.nombreLlamada = nombreLlamada;
    }

    public void findId(String number) {
        String countryCode = getCountryZipCode(this);
        String numeroPreparado = prepararNumero(number, countryCode);
        numeroPreparado = countryCode + numeroPreparado;
        for (int a = 0; a < ServiceBoot.contactos.size(); a++) {
            if (ServiceBoot.contactos.get(a).getTelefono().equals(numeroPreparado)) {
                setIdLlamada(Long.valueOf(ServiceBoot.contactos.get(a).getID()).longValue());
                setNombreLlamada(ServiceBoot.contactos.get(a).getNombreOriginal());
                //   Toast.makeText(getBaseContext(),"OK "+String.valueOf(idLlamada)+nombreLlamada, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String prepararNumero(String numero, String countryCode) {
        numero = numero.replace(countryCode, "");
        return numero.replace(" ", "");
    }

    public String reemplazarCaracteresRaros(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuÁÀÄÉÈËÍÌÏÓÒÖÚÙÜ";
        // Cadena de caracteres ASCII que reemplazar�n los originales.
        String ascii = "aaaeeeiiiooouuuAAAEEEIIIOOOUUU";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }

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
        if (packageExists == false)
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


    public void colgar() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

    public String getCountryZipCode(Context context) {
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

}
