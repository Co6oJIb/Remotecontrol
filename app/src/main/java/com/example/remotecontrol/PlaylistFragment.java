package com.example.remotecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class PlaylistFragment extends Fragment {


    private TextView myTextView3;
    private FragmentActivity    faActivity;
    private RequestQueue queue;
    private SharedPreferences mSettings;
    private String mBytes;




    public void send_request(String var_url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        mParseJSON(response);
                        myTextView3.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                myButton2.setBackgroundResource(android.R.drawable.ic_media_play);
//                mTextView.setText("--:--:--" + " / " + "--:--:--");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                mBytes = ":" + mSettings.getString("password1", "");
                try {
                    params.put("Authorization", "Basic " + Base64.encodeToString(mBytes.getBytes("UTF-8"), Base64.DEFAULT));
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(200, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        faActivity  = (FragmentActivity) super.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        queue = Volley.newRequestQueue(faActivity);
        myTextView3 = (TextView) rootView.findViewById(R.id.textView3);

        send_request("http://192.168.0.65:8080/requests/playlist.json");

        return rootView;

    }

}
