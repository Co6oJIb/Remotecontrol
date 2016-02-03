package com.example.remotecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlaylistFragment extends Fragment {


    public static TextView myTextView3;
    private List<String> mPLArray = new ArrayList<>();
//    private List<String> mFilesArray = new ArrayList<>();
    private JSONObject mSubJObject = new JSONObject();
    public static ProgressBar myProgressBar;
    private FragmentActivity    faActivity;
    private RequestQueue queue;
    private SharedPreferences mSettings;
    private String mBytes;


    public void mParseJSON_add_files(String mResponse) {
        try {
            JSONObject mjObject1 = new JSONObject(mResponse);
            List<String> mDirsArray = new ArrayList<>();
            List<String> mFilesArray = new ArrayList<>();
            JSONArray mjArray = mjObject1.getJSONArray("element");
            for (int i=0; i<mjArray.length(); i++ ) {
                if (mjArray.getJSONObject(i).getString("type").matches("dir") & !mjArray.getJSONObject(i).getString("name").matches("^..$")) {
                    mDirsArray.add(mjArray.getJSONObject(i).getString("name"));
                }
                if (mjArray.getJSONObject(i).getString("type").matches("file")) {
                    mFilesArray.add(mjArray.getJSONObject(i).getString("name"));
                }
            }
            String mDirs = new String();
            mDirs = "Dirs:\n";
            String mFiles = new String();
            mFiles = "Files:\n";
            for (int i=0; i<mDirsArray.size(); i++ ) {
                mDirs = mDirs + "  " + mDirsArray.get(i).toString() + "\n";
            }
            for (int i=0; i<mFilesArray.size(); i++ ) {
                mFiles = mFiles + "  " + mFilesArray.get(i).toString() + "\n";
            }
            myTextView3.setText(mDirs + mFiles);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void mParseJSON_playlist(String mResponse) {
        try {

            JSONObject mjObject1 = new JSONObject(mResponse);
            JSONArray mjArray = mjObject1.getJSONArray("children");
            mPLArray.clear();
            for (int i=0; i<mjArray.length(); i++ ) {
                if (mjArray.getJSONObject(i).getString("name").matches("Playlist")) {
                    for (int k=0; k<mjArray.getJSONObject(i).getJSONArray("children").length(); k++ ) {
                        mPLArray.add(mjArray.getJSONObject(i).getJSONArray("children").getJSONObject(k).getString("name"));
                    }
                }
            }
            String mFiles = new String();
            String mName = "";
            for (int i=0; i<mPLArray.size(); i++ ) {
                mFiles = mFiles + mPLArray.get(i).toString() + "\n";
            }
            try {
                mName = new String(mFiles.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
            }

            myTextView3.setText(mName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void send_request(String var_url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mParseJSON_playlist(response);
//                        myTextView3.setText(response);
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
//        myProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        send_request("http://192.168.0.39:8080/requests/playlist.json");

        return rootView;

    }

}
