package com.Xtian.Blindroid;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;


public class MainActivity extends PreferenceActivity {
	
	
	 
	public boolean inicio;

    private static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT < 16) view.setBackgroundDrawable(background);
        else view.setBackground(background);
    }

    private static Drawable getWindowBackgroundLayer(Drawable windowBackground, int layerId, String layerIdName) {
        if (!(windowBackground instanceof LayerDrawable))
            throw new IllegalStateException("Window background must be a LayerDrawable.");
        final Drawable layer = ((LayerDrawable) windowBackground).findDrawableByLayerId(layerId);
        if (layer == null) throw new IllegalStateException(
                String.format("Window background must have layer with android:id=\"@+id/%s\"", layerIdName));
        return layer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	  //Cargamos el fragment que contiene las opciones
	  actualizarEstadoServicio();


        addPreferencesFromResource(R.xml.opciones);

        final Resources res = getResources();
        final boolean isKitkat = Build.VERSION.SDK_INT == 19;

      // replace window background to reduce overdraw
      final Window window = getWindow();
      final ViewGroup contentView = (ViewGroup)findViewById(android.R.id.content);
      final View content = contentView.getChildAt(0);
      final Drawable extendedWindowBackground = window.getDecorView().getBackground();
      final Drawable windowBackground = !isKitkat ? extendedWindowBackground
                                                  : getWindowBackgroundLayer(extendedWindowBackground,
                                                                             R.id.window_background,
                                                                             "window_background");
      window.setBackgroundDrawable(null);
      setBackground(content, windowBackground);

      // add statusbar background
      if (isKitkat)
      {
          // check if translucent bars are enabled
          final int config_enableTranslucentDecor_id =
                  res.getIdentifier("config_enableTranslucentDecor", "bool", "android");
          if (config_enableTranslucentDecor_id > 0 && res.getBoolean(config_enableTranslucentDecor_id))
          {
              // get ActionBar container
              final View actionBarContainer = findViewById("action_bar_container", "android");
              if (actionBarContainer != null)
              {
                  // add layout listener (can't get margin before layout)
                  //noinspection ConstantConditions
                  actionBarContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                          .OnGlobalLayoutListener()
                  {
                      @SuppressWarnings("ConstantConditions")
                      @Override
                      public void onGlobalLayout()
                      {
                          // remove layout listener
                          final ViewTreeObserver vto = actionBarContainer.getViewTreeObserver();
                          if (Build.VERSION.SDK_INT < 16)
                              vto.removeGlobalOnLayoutListener(this);
                          else vto.removeOnGlobalLayoutListener(this);

                          // create and add statusbar background view
                          final Drawable statusBarBackground = getWindowBackgroundLayer(extendedWindowBackground,
                                                                                        R.id.statusbar_background,
                                                                                        "statusbar_background");
                          final int statusBarHeight =
                                  ((ViewGroup.MarginLayoutParams)actionBarContainer.getLayoutParams()).topMargin;
                          final View statusBarView = new View(MainActivity.this);
                          setBackground(statusBarView, statusBarBackground);
                          final FrameLayout.LayoutParams statusBarBackground_lp = new FrameLayout.LayoutParams(
                                  ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight,
                                  Gravity.TOP | Gravity.FILL_HORIZONTAL);
                          contentView.addView(statusBarView, 0, statusBarBackground_lp);

                          // shift content under actionbar
                          final ViewGroup.MarginLayoutParams content_lp =
                                  (ViewGroup.MarginLayoutParams)content.getLayoutParams();
                          content_lp.topMargin = getActionBar().getHeight() + statusBarHeight;
                          content.setLayoutParams(content_lp);
                      }
                  });
              }
          }
      }


        //Creo el listener para el "bot�n" que inicia y detiene el servicio
      Preference activaServicio = (Preference)findPreference("servicio");

        activaServicio.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(arg0.getContext());
                boolean servicio=prefs.getBoolean("servicio", false);
	    	   if (servicio)
	    	   {
	    		 iniciarServicio();
	    	   } else {
                   detenerServicio();
               }
                return true;
            }


        });


        // Listener para los cambios de sensibilidad
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
	               	 iniciarServicio();
	                }

                return true;
            }


      });
    }

    private void iniciarServicio() {

        Intent i = new Intent(this, ServiceBoot.class);
        i.putExtra("screen_state", false);
		 startService(i);
    }

  private void detenerServicio() {
      stopService(new Intent(this, ServiceBoot.class));
  }

    // funcion que devuelve true o false en funcion de si el servicio esta activo o no
    private boolean isMyServiceRunning() {
		    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.Xtian.Blindroid.ServiceBoot".equals(service.service.getClassName())) {
                    return true;

                }
            }
         return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ClaseGlobal vGlobal = (ClaseGlobal) getApplicationContext();
        boolean busquedaGoogle= vGlobal.existePaquete("com.google.android.googlequicksearchbox");
		  if (!busquedaGoogle)
		  {
			  InstalarBusquedaGoogle();
		  }
        actualizarEstadoServicio();

	 }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.right_in, R.anim.left_out);

    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);

    }

    private void actualizarEstadoServicio() {
        //edito el checkBox para que se adecue al estado del servicio
        boolean servicioActivo = isMyServiceRunning();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (servicioActivo) {

            editor.putBoolean("servicio", true);
        } else {
            editor.putBoolean("servicio", false);
        }
        editor.commit();
    }

    public void InstalarBusquedaGoogle() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.busqueda_google);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.instalar_ahora)
                .setCancelable(false)
                .setPositiveButton(R.string.instalar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.googlequicksearchbox"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id= com.google.android.googlequicksearchbox"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }

                    }
                })
                .setNegativeButton(R.string.despues, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        finish();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.show();
        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);


        // show it
        alertDialog.show();

    }

    public View findViewById(String name, String pkg) {
        final int id = getResources().getIdentifier(name, "id", pkg);
        return id > 0 ? findViewById(id) : null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.tutorial) {
            //Limpio las preferencias.
            SharedPreferences settings = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
            settings.edit().clear().commit();
            //Reinicio la actividad.
            Intent intent = new Intent().setClass(this, SplashScreen.class);
            startActivity(intent);
            detenerServicio();
            finish();
            return true;

        } else if (id == R.id.compartir) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.Xtian.Blindroid");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Prueba mi nueva app: Blindroid, cambiaras tu forma de llamar!");
            startActivity(Intent.createChooser(intent, getString(R.string.compartir)));
        } else if (id == R.id.info) {
            String url = "https://plus.google.com/communities/102190305058584818425";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);


        } else if (id == R.id.contacto) {

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"xtianrock89@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Blindroid");
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent, "Email "));
        }

        return super.onOptionsItemSelected(item);
    }

}