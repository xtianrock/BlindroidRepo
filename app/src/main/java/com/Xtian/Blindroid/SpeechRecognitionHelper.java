package com.Xtian.Blindroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.widget.TextView;

import java.util.List;


/**
 * Clase helper que verifica e inicia el reconocimiento de voz
 */
public class SpeechRecognitionHelper {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    /**
     * Comprueba si esta instalada la busqueda de Google antes de iniciar el reconocimiento de voz
     * @param ownerActivity activity a la que debe devolver los resultados
     * @param promt mensaje que ha de mostrar en el cuadro de dialogo
     */
	public static void run(Activity ownerActivity, int promt) {

		if (isSpeechRecognitionActivityPresented(ownerActivity)) {
            startVoiceRecognitionActivity(ownerActivity, promt);
		} else {
			installGoogleVoiceSearch(ownerActivity);
		}
	}

    /**
     * Comprueba si estan instalada la busqueda de google
     * @param ownerActivity
     * @return
     */
	private static boolean isSpeechRecognitionActivityPresented(Activity ownerActivity) {
        try {

            PackageManager pm = ownerActivity.getPackageManager();

            List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                return true;
            }
        } catch (Exception ignored) {

        }

        return false;
    }


    /**
     *  Inicia el reconocimiento de voz
     * @param activity
     * @param prompt
     */
    public static void startVoiceRecognitionActivity(Activity activity ,int prompt) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,prompt);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }


    /**
     * Instala la busqueda de google
     * @param activity
     */
    private static void installGoogleVoiceSearch (final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);


        alertDialogBuilder.setTitle(R.string.busqueda_google);

        alertDialogBuilder
                .setMessage(R.string.instalar_ahora)
                .setCancelable(false)
                .setPositiveButton(R.string.instalar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.googlequicksearchbox"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(i);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id= com.google.android.googlequicksearchbox"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(i);
                        }

                    }
                })
                .setNegativeButton(R.string.despues, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.show();
        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);

        alertDialog.show();

    }
}
