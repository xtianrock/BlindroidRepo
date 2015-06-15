package com.Xtian.Blindroid;

/**
 * Created by Cristian on 11/08/2014.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que pagina fragments
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    // List of fragments which are going to set in the view pager widget
    List<Fragment> fragments;

    /**
     * Constructor
     *
     * @param fm interfaz para interactuar con fragments dentro de activitys
     */
    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<Fragment>();
    }

    /**
     * Añade un nuevo fragment a la lista
     *
     * @param fragment
     */
    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int arg0) {
        return this.fragments.get(arg0);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Página " + (position + 1);
    }
}