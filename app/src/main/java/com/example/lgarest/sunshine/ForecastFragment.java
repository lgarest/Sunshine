package com.example.lgarest.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    /* Called at the start of the Fragment lifecycle */
    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /* Called when a new instance of ForecastFragment is created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /* Called when the fragment view is created */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        /* Binds the forecast data, the list_item_forecast layout and the
           list_item_forecast_textview */
        forecastAdapter= new ArrayAdapter<String>(
                // pass the context of the activity
                getActivity(),
                // list item layout
                R.layout.list_item_forecast,
                // id of the textview element
                R.id.list_item_forecast_textview,
                // the data default to empty
                new ArrayList<String>());

        // Get the listview_forecast by id and set its adapter
        ListView listView = (ListView) rootView.findViewById(
                R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        // Add a listener when clicking on an item
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // create and override onItemClick behaviour, needs the adapter, the view, the position in the adapter and a long
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String forecast = forecastAdapter.getItem(position);
                // Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show(); // toast used to debug
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    /* Inflates the refresh option when optionsmenu is created */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    /* Called when an option of the menu is selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh){
            updateWeather();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Get the weather forecast data from the preferred location setting */
    private void updateWeather(){
        FetchForecastTask fetchForecastTask = new FetchForecastTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        fetchForecastTask.execute(location);
    }

    /* The date/time conversion code */
    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /* Prepare the weather high/lows for presentation and converted if needed */
    private String formatHighLows(double high, double low) {
        SharedPreferences sharedprefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType = sharedprefs.getString(
                getString(R.string.pref_units_key),
                getString(R.string.pref_units_metric));
        if(unitType.equals(getString(R.string.pref_units_imperial))){
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals((getString(R.string.pref_units_metric)))){
            Log.d("formatHighLows", "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        if (forecastJsonStr == null) return null;

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }

    /* Overrides the AsyncTask class to create our asynchronous task fetching the forecast */
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

                Log.v(LOG_TAG, "url: " + url); // verifies the correctness of the url

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
                return getWeatherDataFromJson(forecastJsonStr, numberOfDays);
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
            } else {
                Toast.makeText(getActivity(), getString(R.string.error_empty_forecast), Toast.LENGTH_SHORT).show(); // toast used to debug
            }
        }
    }

}
