package com.example.remotecontrol;

import android.os.AsyncTask;

import java.io.IOException;

/**
 * Created by Co6oJIb on 11.10.2016.
 */
public class wolAsync extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        try{
            WakeOnLan.send(params[0], params[1], 9);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
