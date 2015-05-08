package com.example.lgarest.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    /**
     * Called when a new instance of ForecastFragment is created
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Inflates the refresh option when optionsmenu is created
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    /**
     * Called when an option of the menu is selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            FetchForecastTask fetchForecastTask = new FetchForecastTask();

            String city = "Barcelona,es";
            fetchForecastTask.execute(city);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the fragment view is created
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

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
        forecastAdapter= new ArrayAdapter<String>(
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

    /**
     * Overrides the AsyncTask class to create our asynchronous task fetching the forecast
     */
    public class FetchForecastTask extends AsyncTask<String, Void, String[]>{

        private String LOG_TAG = FetchForecastTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // Log.v(LOG_TAG, "Created");

            // manages the HTTP connection
            HttpURLConnection urlConnection = null;
            // reading buffer
            BufferedReader reader = null;

            // will contain the forecast json in a str format
            String forecastJsonStr = null;

            int numberOfDays = 7;  // # de días de la previsión
            String units = "metric";    // formato de las unidades de temperatura
            String format = "json";     // formato de los datos

            try{

                // base url
                String BASEWEATHERAPIURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

                // API call parameters constants
                String QUERYPARAM = "q";
                String FORMATPARAM = "mode";
                String UNITSPARAM = "units";
                String DAYSPARAM = "cnt";


                // construction of the URL with parameters
                Uri builtUrl = Uri.parse(BASEWEATHERAPIURL).buildUpon()
                        .appendQueryParameter(QUERYPARAM, params[0])
                        .appendQueryParameter(FORMATPARAM, format)
                        .appendQueryParameter(UNITSPARAM, units)
                        .appendQueryParameter(DAYSPARAM, String.valueOf(numberOfDays))
                        .build();

                // url object with the built url
                URL url = new URL(builtUrl.toString());

                // Log.v(LOG_TAG, "url: " + url); // verifies the correctness of the url

                // opens the conection, sets the method as GET and make the request
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // get the content from the response
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                // if no data -> return
                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null){ buffer.append(line + "\n"); }

                if (buffer.length() == 0){ return null; }
                forecastJsonStr = buffer.toString();

                // Log.d(LOG_TAG, "forecastJsonStr: " + forecastJsonStr); // verifies the correctness of the JSON
            } catch (IOException e){
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) urlConnection.disconnect(); // disconnect
                if (reader != null){
                    try { reader.close(); }
                    catch (final IOException e) { Log.e(LOG_TAG, "Error closing stream", e); }
                }
            }
            try{
                return WeatherParser.getWeatherDataFromJson(forecastJsonStr, numberOfDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // Log.v(LOG_TAG, "ForecastFragment inflate");
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null){
                forecastAdapter.clear();
                for (String dayForecastStr:result){
                    forecastAdapter.add(dayForecastStr);
                } // new data added from the server
            }
        }
    }
}
