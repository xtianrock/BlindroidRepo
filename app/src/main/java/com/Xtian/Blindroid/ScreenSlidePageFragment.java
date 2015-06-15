package com.Xtian.Blindroid;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


/**
 * Fragments que seran usados dentro del pager
 */
public class ScreenSlidePageFragment extends Fragment {


    private static final String BACKGROUND_COLOR = "color";
    private static final String INDEX = "index";
    ViewGroup rootView;
    private int color;
    private int index;
    private String text;
    private String action;

    /**
     * Intancia un nuevo fragment con un color de fondo y un numeor de pagina
     *
     * @param color
     * @param index
     * @return
     */
    public static ScreenSlidePageFragment newInstance(int color, int index, String text,String action) {

        // Instantiate a new fragment
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();

        // Save the parameters
        Bundle bundle = new Bundle();
        bundle.putInt(BACKGROUND_COLOR, color);
        bundle.putInt(INDEX, index);
        bundle.putString("texto", text);
        bundle.putString("action", action);
        fragment.setArguments(bundle);
        fragment.setRetainInstance(true);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load parameters when the initial creation of the fragment is done
        this.color = (getArguments() != null) ? getArguments().getInt(
                BACKGROUND_COLOR) : Color.GRAY;
        this.index = (getArguments() != null) ? getArguments().getInt(INDEX)
                : -1;
        this.text = (getArguments() != null) ? getArguments().getString("texto")
                : "";
        this.action = (getArguments() != null) ? getArguments().getString("action")
                : "";
        Tracker t = ((Commons) getActivity().getApplication()).getTracker(Commons.TrackerName.APP_TRACKER);
        t.setScreenName("Tutorial");
        t.send(new HitBuilders.ScreenViewBuilder().build());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(
                R.layout.splash_layout, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textView);
        tv.setText(text);

        // Change the background color
        rootView.setBackgroundColor(this.color);
        if (this.index == 7) {
            ImageView iv = (ImageView) rootView.findViewById(R.id.imageView2);
            iv.setVisibility(View.GONE);
            Button bt = (Button) rootView.findViewById((R.id.button));
            bt.setVisibility(View.VISIBLE);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startApp(action);
                }
            });
        }

        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }

    /**
     * Inicia una activity u otra en funcion de la accion pasada como parametro
     * @param action
     */
    private void startApp(String action)
    {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String phone=prefs.getString("phoneNumber", "");
        Intent intent;
        if(action.equals("tutorial"))
        {
            intent = new Intent(this.getActivity(),MainActivity.class);
        }
        else if(phone.equals(""))
        {
            intent = new Intent(this.getActivity(), PhoneConfirmationActivity.class);
        }
        else
        {
            intent = new Intent(this.getActivity(), SettingsActivity.class);
        }
        startActivity(intent);
        getActivity().finish();
    }



}