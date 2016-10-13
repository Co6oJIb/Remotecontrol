package com.example.remotecontrol;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class mPrefs extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref1);
        Preference myPref = findPreference( "ThirdPrefScreen" );
        myPref.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener()
        {
            public boolean onPreferenceClick( Preference pref )
            {
                updateAsync mUpdate = new updateAsync();
                mUpdate.setContext(getActivity());
                mUpdate.execute("http://192.168.88.250/Remotecontrol.apk");
                return true;
            }
        } );
    }

}