package com.Xtian.Blindroid;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;

public class ActivityColgar extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reconocimiento);

        Intent intent = getIntent();

        int metodo = intent.getIntExtra("metodo", 5);

        if (metodo == 0) {
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
            finish();
        } else if (metodo == 1) {
            speakerToggle();
        }


    }
  /* protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(getBaseContext(),intent.getStringExtra("metodo"),Toast.LENGTH_SHORT).show();
            if(intent.getStringExtra("metodo").equals("colgar"))
            {

                try {
                    colgar();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if(intent.getStringExtra("metodo").equals("speaker"))
            {
                speakerToggle();
            }
        }
*/


    private void speakerToggle() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);
        } else {
            mAudioManager.setSpeakerphoneOn(true);
        }
        finish();
    }
}
