package com.Xtian.Blindroid;

/**
 * Created by Cristian on 26/07/2014.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Splash screen
 */
public class SplashScreenActivity extends FragmentActivity {

    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 1000;
    private static final long FINISH_DELAY = 400;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.prueba_pager);
        View screen = this.findViewById(android.R.id.content);
        String action=getIntent().getAction();


// Instantiate a ViewPager
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager);

        // Set a custom animation
        //  pager.setPageTransformer(true, new ZoomOutPageTransformer());

        // Create an adapter with the fragments we show on the ViewPager
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(
                getSupportFragmentManager());
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.green), 1, getString(R.string.tuto1),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.cyan), 2, getString(R.string.tuto2),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.blue), 3, getString(R.string.tuto3),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.purple), 4, getString(R.string.tuto4),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.pink), 5, getString(R.string.tuto5),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.red), 6, getString(R.string.tuto6),null));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.orange), 7, getString(R.string.tuto7),action));


        pager.setAdapter(adapter);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startMainActivity();
            }
        };

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        final Boolean firstTime = prefs.getBoolean("firstTime", true);
        final String phoneNumber = prefs.getString("phone", "");

        if (!firstTime) {
           if (TextUtils.isEmpty(phoneNumber))
            {
                Intent mainIntent = new Intent(this, PhoneConfirmationActivity.class);
                startActivity(mainIntent);
                finish();
            }
           else if(!getIntent().getAction().equals("tutorial"))
           {
               setContentView(R.layout.splash_layout_inicio);
               Timer timer = new Timer();
               timer.schedule(task, SPLASH_SCREEN_DELAY);
           }
        }
        else {
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean("firstTime", false);
            e.apply();
        }
    }

    /**
     * Inicia la actividad principal
     */
    private void startMainActivity() {
        Intent i = new Intent(this, BlindroidService.class);
        i.putExtra("screen_state", false);
        startService(i);
        Intent mainIntent = new Intent().setClass(this, MainActivity.class);
        startActivity(mainIntent);
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task2, FINISH_DELAY);

    }


    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


}
