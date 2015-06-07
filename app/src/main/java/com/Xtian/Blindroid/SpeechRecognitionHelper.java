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


public class SpeechRecognitionHelper {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

	public static void run(Activity ownerActivity, int promt) {

		if (isSpeechRecognitionActivityPresented(ownerActivity)) {
            startVoiceRecognitionActivity(ownerActivity, promt);
		} else {
			installGoogleVoiceSearch(ownerActivity);
		}
	}


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


    public static void startVoiceRecognitionActivity(Activity activity ,int prompt) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,prompt);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
	


    private static void installGoogleVoiceSearch (final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

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
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        //finish();
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
}
