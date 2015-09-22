package com.example.remotecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TorrentFragment extends Fragment {
    private FragmentActivity faActivity;
    private TableLayout myTable;
    private RequestQueue queue;
    private String sessionId;
    private List<String> mTorrentsArray = new ArrayList<>();
    private SharedPreferences mSettings;
    private String myUrl;
    private JSONObject myResponse;
    private Handler mHandler = new Handler();

    private Runnable mUpdateTorrentsList = new Runnable() {

        @Override
        public void run() {
            listTorrents();
            mHandler.postDelayed(this, 1000);
        }
    };
    public void updateTorrentsList() {
        mHandler.postDelayed(mUpdateTorrentsList, 100);
    }

    public void myParseJSON(JSONObject response) {
        mTorrentsArray.clear();
        try {
            JSONObject mjObject = response.getJSONObject("arguments");
            JSONArray mjArray = mjObject.getJSONArray("torrents");;
            for (int i=0; i<mjArray.length(); i++) {
                mTorrentsArray.add(mjArray.getJSONObject(i).getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        myDrawTable();
    }
    public void addTorrent(byte[] torrentFile) {
        JSONObject myReqParams = new JSONObject();
        JSONObject myArguments = new JSONObject();
        try {
//            myArguments.put("metainfo", Base64.encodeToString(torrentFile, Base64.DEFAULT));
            myArguments.put("filename", "http://ru-tor.net/download/458581");
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-add");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request(myUrl, myReqParams, false);
    }
    public void listTorrents() {
        JSONObject myReqParams = new JSONObject();
        JSONObject myArguments = new JSONObject();
        JSONArray myFields = new JSONArray();
        myFields.put("name");
        try {
            myArguments.put("fields", myFields);
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-get");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request(myUrl, myReqParams, true);
    }
    public void myDrawTable() {
        myTable.removeAllViews();
        for (int i = 0; i < mTorrentsArray.size(); i++ ) {
            TextView myTextView1 = new TextView(getActivity());
            myTextView1.setText(mTorrentsArray.get(i));
            TableRow row1 = new TableRow(getActivity());
            row1.setId(1000 + i);
            RelativeLayout myRLayout = new RelativeLayout(getActivity());
            myRLayout.addView(myTextView1);
            RelativeLayout.LayoutParams rLParams = (RelativeLayout.LayoutParams)myTextView1.getLayoutParams();
            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            myTextView1.setLayoutParams(rLParams);
            myRLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, dpToPx(80)));
            row1.addView(myRLayout);
            View mySeparator = new View(getActivity());
            mySeparator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, dpToPx(1)));
            mySeparator.setBackgroundColor(Color.argb(255, 195, 195, 195));
            mySeparator.setId(1000 + i);

            myTable.addView(row1);
            myTable.addView(mySeparator);
        }
        View myWSpace = new View(getActivity());
        myWSpace.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, dpToPx(60)));
        myTable.addView(myWSpace);
    }

    public void send_request(String var_url, final JSONObject myReqParams, final Boolean... parse) {
        JsonObjectRequest myJsonRequest = new JsonObjectRequest(Request.Method.POST, var_url, myReqParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (parse[0] == true) {
                        myParseJSON(response);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("X-Transmission-Session-Id", sessionId);

                    return params;
                }
                @Override
                protected VolleyError parseNetworkError(VolleyError volleyError){
                    if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
                        sessionId = volleyError.networkResponse.headers.get("X-Transmission-Session-Id");
                    }
                    return volleyError;
                }
            };
        myJsonRequest.setRetryPolicy(new DefaultRetryPolicy(200, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myJsonRequest);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_torrent, container, false);
        faActivity  = (FragmentActivity)    super.getActivity();

        queue = Volley.newRequestQueue(faActivity);
        mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        myUrl = "http://" + mSettings.getString("address1", "") + ":9091/transmission/rpc";

        myTable = (TableLayout) rootView.findViewById(R.id.tableLayout);

        updateTorrentsList();

        return rootView;
    }

}