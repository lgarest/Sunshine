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

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }


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

    public class FetchForecastTask extends AsyncTask<String, Void, Void>{

        private String LOG_TAG = FetchForecastTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            Log.e(LOG_TAG, "ForecastFragment created");

            // clase que gestiona la conexión a HTTP
            HttpURLConnection urlConnection = null;
            // búffer de lectura
            BufferedReader reader = null;

            // contendrá el json de la previsión
            String forecastJsonStr = null;

            String numberOfDays = "7";  // # de días de la previsión
            String units = "metric";    // formato de las unidades de temperatura
            String format = "json";     // formato de los datos

            try{

                // url base sobre la que haremos la llamada
                String BASEWEATHERAPIURL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

                // constantes para los parámetros de la llamada a la API
                String QUERYPARAM = "q";
                String FORMATPARAM = "mode";
                String UNITSPARAM = "units";
                String DAYSPARAM = "cnt";


                // construcción de la URL con los parámetros
                Uri builtUrl = Uri.parse(BASEWEATHERAPIURL).buildUpon()
                        .appendQueryParameter(QUERYPARAM, params[0])
                        .appendQueryParameter(FORMATPARAM, format)
                        .appendQueryParameter(UNITSPARAM, units)
                        .appendQueryParameter(DAYSPARAM, numberOfDays)
                        .build();

                // creamos la url como un string a partir del string de la url construida
                URL url = new URL(builtUrl.toString());

                Log.e(LOG_TAG, "url: " + url);

                // abre la conexión, especifica el método como GET y conecta
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // recoge lo que devuelva la conexión
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                // si no hemos recibido datos return
                if (inputStream == null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0){
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.d(LOG_TAG, "forecastJsonStr: " + forecastJsonStr);
            } catch (IOException e){
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null){
                    try {
                        reader.close();
                    } catch (final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.e(LOG_TAG, "ForecastFragment inflate");
            return null;
        }

    }
}
