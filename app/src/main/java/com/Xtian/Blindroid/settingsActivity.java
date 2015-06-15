package com.Xtian.Blindroid;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Muestra una pantalla con los ajustes de la app
 * En las nuevas version de android existen nuevas apis
 * para tratar con las preferencias mediante el uso de fragments,
 * por tanto tendre que actualizar este codigo proximamente.
 */
public class SettingsActivity extends PreferenceActivity {
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.opciones);

        final ListPreference lp = (ListPreference) getPreferenceManager().findPreference("sensibilidad");
      lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
          	  	SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(preference.getContext());
	                boolean servicio=prefs.getBoolean("servicio", false);
	                if (servicio)
	                {
	                	//Reinicio el servicio para actualizar la sensibilidad
                        detenerServicio();
	               	    iniciarServicio();
	                }

                return true;
            }
      });
    }

    /**
     * Inicia el servicio BlindroidService
     */
    private void iniciarServicio() {

        Intent i = new Intent(this, BlindroidService.class);
        i.putExtra("screen_state", false);
		 startService(i);
    }

    /**
     * Detiene el servicio BlindroidService
     */
    private void detenerServicio() {
        stopService(new Intent(this, BlindroidService.class));
    }

    /**
     * Comprueba si el servicio se esta ejecutando.
     *
     * @return true si esta activo, false en caso contrario.
     */
    private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.Xtian.Blindroid.BlindroidService".equals(service.service.getClassName())) {
                    return true;

                }
            }
         return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
	 }

    @Override
    public void onStart() {
        super.onStart();
        Tracker t = ((Commons) getApplication()).getTracker(Commons.TrackerName.APP_TRACKER);
        t.setScreenName("MainActivity");
        t.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

    }

    @Override
    public void onStop() {
        super.onStop();
    }



}