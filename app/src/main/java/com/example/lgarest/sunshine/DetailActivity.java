package com.example.lgarest.sunshine;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class DetailActivity extends ActionBarActivity {
    private String LOG_TAG = DetailActivity.class.getSimpleName();
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        inflater.inflate(R.menu.menu_detail, menu);
//
//        // get the action_share menuitem
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        // set a ShareActionProvider from the previous action_share menuitem
//        ShareActionProvider mShareActionProvider =
//                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        // if everything works as expected, we set the share intent from the shareforecastintent
//        if (mShareActionProvider != null){
//            mShareActionProvider.setShareIntent(DetailActivityFragment.createShareForecastIntent());
//        } else {
//            Log.d(LOG_TAG, "Share provider is null?");
//        }
//        return true;
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_detail, menu);
//
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // if action_settings selected, start the SettingsActivity activity
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
