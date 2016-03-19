package com.example.remotecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дмитрий on 03.02.2016.
 */

public class PLArrayAdapter extends ArrayAdapter<PLitem> {
    public PLArrayAdapter(Context context, ArrayList<PLitem> PLitems) {
        super(context, 0, PLitems);
    }
    public void send_request(String var_url,final Boolean... parse) {
        Activity faActivity  = (Activity) super.getContext();
        final SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        final RequestQueue queue = Volley.newRequestQueue(faActivity);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                final String mBytes = ":" + mSettings.getString("password1", "");
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PLitem plitem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pl_text_view, parent, false);
        }
        // Lookup view for data population
        TextView PLitemName = (TextView) convertView.findViewById(R.id.textView3);
        // Populate the data into the template view using the data object
        PLitemName.setText(plitem.name);
        final String url = "http://192.168.0.39:8080/requests/status.json?command=pl_play&id=" + plitem.id;
        PLitemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request(url, false);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}