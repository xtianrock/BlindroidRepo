package com.Xtian.Blindroid;

/**
 * Created by Cristian on 11/08/2014.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * @author amatellanes
 */
public class ScreenSlidePageFragment extends Fragment {

    /**
     * Key to insert the background color into the mapping of a Bundle.
     */
    private static final String BACKGROUND_COLOR = "color";

    /**
     * Key to insert the index page into the mapping of a Bundle.
     */
    private static final String INDEX = "index";
    ViewGroup rootView;
    private int color;
    private int index;
    private String text;
    private Layout screen;

    /**
     * Instances a new fragment with a background color and an index page.
     *
     * @param color background color
     * @param index index page
     * @return a new page
     */
    public static ScreenSlidePageFragment newInstance(int color, int index, String text) {

        // Instantiate a new fragment
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();

        // Save the parameters
        Bundle bundle = new Bundle();
        bundle.putInt(BACKGROUND_COLOR, color);
        bundle.putInt(INDEX, index);
        bundle.putString("texto", text);
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(
                R.layout.splash_layout, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.textView);
        tv.setText(text);

        // Change the background color
        rootView.setBackgroundColor(this.color);
        if (this.index == 6) {
            ImageView iv = (ImageView) rootView.findViewById(R.id.imageView2);
            iv.setVisibility(View.GONE);
            Button bt = (Button) rootView.findViewById((R.id.button));
            bt.setVisibility(View.VISIBLE);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gAnalyticTutorial();
                    iniciarAjustes();
                    iniciarServicio();
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


    private void iniciarAjustes() {
        Intent mainIntent = new Intent(this.getActivity(), MainActivity.class);
        startActivity(mainIntent);
        getActivity().finish();


    }

    private void iniciarServicio() {

        Intent i = new Intent(this.getActivity(), ServiceBoot.class);
        i.putExtra("screen_state", false);
        getActivity().startService(i);
    }

    public void gAnalyticTutorial() {
        EasyTracker easyTracker = EasyTracker.getInstance(this.getActivity());

        // MapBuilder.createEvent().build() returns a Map of event fields and values
        // that are set and sent with the hit.
        easyTracker.send(MapBuilder
                        .createEvent("Splash_screen",     // Event category (required)
                                "Tutorial",  // Event action (required)
                                "Tutorial_completado",   // Event label
                                null)            // Event value
                        .build()
        );
    }

}