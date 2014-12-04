package com.Xtian.Blindroid;

/**
 * Created by Cristian on 26/07/2014.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends FragmentActivity {

    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 1500;
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

        SharedPreferences prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        final Boolean tutorial = prefs.getBoolean("PrimeraVez", true);


// Instantiate a ViewPager
        ViewPager pager = (ViewPager) this.findViewById(R.id.pager);

        // Set a custom animation
        //  pager.setPageTransformer(true, new ZoomOutPageTransformer());

        // Create an adapter with the fragments we show on the ViewPager
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(
                getSupportFragmentManager());
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.verde), 1, getString(R.string.tuto1)));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.celeste), 2, getString(R.string.tuto2)));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.naranja), 3, getString(R.string.tuto3)));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.morado), 4, getString(R.string.tuto4)));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.rosa), 5, getString(R.string.tuto5)));
        adapter.addFragment(ScreenSlidePageFragment.newInstance(getResources()
                .getColor(R.color.rojo), 6, getString(R.string.tuto6)));


        pager.setAdapter(adapter);


        final Intent i = new Intent(this, ServiceBoot.class);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                iniciarAjustes();

            }

        };

        if (!tutorial) {
            setContentView(R.layout.splash_layout_inicio);
            Timer timer = new Timer();
            timer.schedule(task, SPLASH_SCREEN_DELAY);
        } else {
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean("PrimeraVez", false);
            e.commit();

        }


    }


    private void iniciarAjustes() {
        Intent mainIntent = new Intent().setClass(this, MainActivity.class);
        startActivity(mainIntent);
        iniciarServicio();
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task2, FINISH_DELAY);


    }

    private void iniciarServicio() {

        Intent i = new Intent(this, ServiceBoot.class);
        i.putExtra("screen_state", false);
        startService(i);
    }

    protected void onPause() {
        super.onPause();

        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


}
