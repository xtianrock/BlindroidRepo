package com.Xtian.Blindroid;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;

import static com.Xtian.Blindroid.BlindroidService.ENDCALL;
import static com.Xtian.Blindroid.BlindroidService.SPEAKER;

/**
 * Activity que sera llamada por las acciones de la notificacion wear
 */
public class PhoneActionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reconocimiento);

        Intent intent = getIntent();
        String action = intent.getAction();

        switch (action)
        {
            case ENDCALL:
                final Commons commons = (Commons) getApplicationContext();
                try {
                    commons.endCall();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            case SPEAKER:
                speakerToggle();
                break;
        }
    }

    /**
     * Conmuta el estado del manos libres
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
