package com.example.remotecontrol;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class mPrefsFragment extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_prefs_fragment);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new mPrefs()).commit();
    }
}
