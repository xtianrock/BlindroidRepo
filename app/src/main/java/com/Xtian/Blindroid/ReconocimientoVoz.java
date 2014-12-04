package com.Xtian.Blindroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class ReconocimientoVoz extends Activity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;
    Dialog customDialog;
    boolean existe;
    ClaseGlobal vGlobal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.reconocimiento);

        Log.i("xtian", "oncreate");

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startVoiceRecognitionActivity();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 150);


    }


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    protected void onResume() {
        super.onResume();
        //  Log.i("xtian", "onResume");
    }


    public void startVoiceRecognitionActivity() {
        // Log.i("xtian", "reconocimiento activado");

        // Definici�n del intent para realizar en an�lisis del mensaje
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Indicamos el modelo de lenguaje para el intent
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Definimos el mensaje que aparecer�
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.prompt);
        // Lanzamos la actividad esperando resultados
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    //Recogemos los resultados del reconocimiento de voz
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Si el reconocimiento a sido bueno
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            //El intent nos envia un ArrayList aunque en este caso solo

            ArrayList<String> resultados = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            vGlobal = (ClaseGlobal) getApplicationContext();


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ReconocimientoVoz.this);

            modoDepuracion(resultados, prefs);

            int length = resultados.size();

            existe = false;
            bucle:
            for (int i = 0; i < length; i++) {
                String nombre = vGlobal.reemplazarCaracteresRaros(resultados.get(i)).toLowerCase();

                for (int a = 0; a < ServiceBoot.contactos.size(); a++) {

                    if (ServiceBoot.contactos.get(a).getNombre().equals(nombre)) {

                        existe = true;
                        vGlobal.setLlamando(true);
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + ServiceBoot.contactos.get(a).getTelefono()));
                        startActivity(callIntent);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(200);
                        gAnalyticCall();
                        finish();
                        break bucle;
                    }

                }

            }

            if (!existe) {
                mostrarDialog(resultados);
            }


        } else {

            finish();
        }
    }


    private void modoDepuracion(ArrayList<String> resultados, SharedPreferences prefs) {
        boolean depuracion = prefs.getBoolean("depuracion", false);
        if (depuracion) {
            String texto = "";
            for (int i = 0; i < resultados.size(); i++) {
                texto += " " + resultados.get(i);
            }
            Toast.makeText(getBaseContext(), texto, Toast.LENGTH_SHORT).show();
        }
    }


    protected void onPause() {
        super.onPause();


    }


    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
        finish();
    }

    private void mostrarDialog(ArrayList nombres) {
        // con este tema personalizado evitamos los bordes por defecto
        customDialog = new Dialog(this, R.style.Theme_Dialog_Translucent);
        //deshabilitamos el título por defecto
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //obligamos al usuario a pulsar los botones para cerrarlo
        customDialog.setCancelable(false);
        //establecemos el contenido de nuestro dialog
        customDialog.setContentView(R.layout.dialog);

        TextView contenido = (TextView) customDialog.findViewById(R.id.contenido);

        for (int i = 0; i < nombres.size(); i++) {
            if (i < nombres.size() - 1) {
                contenido.setText((contenido.getText() + " " + nombres.get(i) + ","));
            } else {
                contenido.setText((contenido.getText() + " " + nombres.get(i) + "."));
            }

        }
        if (!isOnline()) {
            TextView internet = (TextView) customDialog.findViewById(R.id.internet);
            internet.setVisibility(View.VISIBLE);

            View separador = (View) customDialog.findViewById(R.id.divider2);
            separador.setVisibility(View.VISIBLE);
        }
        ((Button) customDialog.findViewById(R.id.aceptar)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                startVoiceRecognitionActivity();

            }
        });

        ((Button) customDialog.findViewById(R.id.cancelar)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                customDialog.dismiss();
                finish();

            }
        });

        customDialog.show();
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void gAnalyticCall() {
        EasyTracker easyTracker = EasyTracker.getInstance(this);

        // MapBuilder.createEvent().build() returns a Map of event fields and values
        // that are set and sent with the hit.
        easyTracker.send(MapBuilder
                        .createEvent("Reconocimiento",     // Event category (required)
                                "Llamada",  // Event action (required)
                                "Llamada_Blindroid",   // Event label
                                null)            // Event value
                        .build()
        );
    }

}