package com.example.remotecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Дмитрий on 03.02.2016.
 */

public class LoLStreamsArrayAdapter extends ArrayAdapter<LoLStream> {
    public LoLStreamsArrayAdapter(Context context, ArrayList<LoLStream> LoLStreams) {
        super(context, 0, LoLStreams);
    }
    public void send_request(String var_url, final String mChannel, final boolean parse) {
        Activity faActivity  = (Activity) super.getContext();
        final SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        final RequestQueue queue = Volley.newRequestQueue(faActivity);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, var_url,
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
                final String mBytes = "co6ojib:" + mSettings.getString("password1", "");
                try {
                        params.put("Authorization", "Basic " + Base64.encodeToString(mBytes.getBytes("UTF-8"), Base64.DEFAULT));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String key = "channel";
                params.put(key, mChannel);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(200, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final LoLStream lolstream = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lol_streams_text_view, parent, false);
        }
        // Lookup view for data population
        ImageView LoLStreamIcon = (ImageView) convertView.findViewById(R.id.imageView);
        int resID= getContext().getResources().getIdentifier(lolstream.name.toLowerCase(), "drawable", getContext().getPackageName());
        LoLStreamIcon.setImageResource(resID);
        TextView LoLStreamName = (TextView) convertView.findViewById(R.id.textView3);
        LoLStreamName.setText(lolstream.cName);
        // Populate the data into the template view using the data object
        final String url = "http://192.168.88.250/riot.php";
        LoLStreamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request(url, lolstream.channel, false);
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}