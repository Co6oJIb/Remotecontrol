package com.example.remotecontrol;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class mPrefs extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref1);
    }

}