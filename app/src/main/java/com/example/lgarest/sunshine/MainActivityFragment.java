package com.example.lgarest.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Dummy data for the ListView
        String[] dummy_forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51",
                "sun - Sunny - 80/68"
        };

        // List with the Dummy data
        List<String> dummy_weekForecast = new ArrayList<String>(
                Arrays.asList(dummy_forecastArray));

        // Binds the forecast dummy data, the list_item_forecast layout and the
        //   list_item_forecast_textview
        ArrayAdapter<String> forecastAdapter= new ArrayAdapter<String>(
                // pass the context of the activity
                getActivity(),
                // id of the list item layout
                R.layout.list_item_forecast,
                // id of the textview element
                R.id.list_item_forecast_textview,
                // the data
                dummy_weekForecast);

        // Get the listview_forecast by id and set its adapter
        ListView listView = (ListView) rootView.findViewById(
                R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        return rootView;
    }
}
