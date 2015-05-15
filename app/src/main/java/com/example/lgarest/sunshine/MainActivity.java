package com.example.lgarest.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // check wich option has been selected
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.action_display_location) {
            openPreferredLocationInMap();
        }


        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
        // get the saved settings
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedprefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        // building of the Google Maps Intent Uri containing the location
        Uri gmIntentUri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location).build();

        // mapIntent will start the maps Activity
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(gmIntentUri);
        if (mapIntent.resolveActivity(getPackageManager()) != null){
            startActivity(mapIntent);
        } else {
            Log.d(LOG_TAG, "Couldn't call " + location + " no map app installed!");
        }
    }
}
