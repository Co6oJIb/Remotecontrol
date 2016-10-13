package com.example.remotecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.example.remotecontrol.PLArrayAdapter;
import com.example.remotecontrol.PLitem;
import com.example.remotecontrol.PlaybackFragment;
import com.example.remotecontrol.R;
import com.example.remotecontrol.addPLitem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LoL_StreamsFragment extends Fragment {

    private FragmentActivity faActivity;
    private ListView mlistView;
    private RequestQueue queue;
    private ArrayList<String> channel_LCK = new ArrayList<>();
    private ArrayList<String> channel_EULCS = new ArrayList<>();
    private ArrayList<String> channel_NALCS = new ArrayList<>();
    private ArrayList<String> channel_LPL = new ArrayList<>();
    private ArrayList<String> channel_LMS = new ArrayList<>();
    private ArrayList<String> channel_RIOT = new ArrayList<>();
    private ArrayList<ArrayList<String>> mLoLStreamsArr = new ArrayList<>();
    private SwipeRefreshLayout mSwipeLayout;
    public void drawList(){
        String twitch_api_url = "https://api.twitch.tv/kraken/streams/";
        mLoLStreamsArr.clear();
        channel_LCK.clear();
        channel_LCK.add("LCK");
        channel_LCK.add("lck1");
        send_request(twitch_api_url + "lck1", channel_LCK);
        channel_EULCS.clear();
        channel_EULCS.add("EULCS");
        channel_EULCS.add("eulcs1");
        send_request(twitch_api_url + "eulcs1", channel_EULCS);
        channel_NALCS.clear();
        channel_NALCS.add("NALCS");
        channel_NALCS.add("nalcs1");
        send_request(twitch_api_url + "nalcs1", channel_NALCS);
        channel_LPL.clear();
        channel_LPL.add("LPL");
        channel_LPL.add("lpl1");
        send_request(twitch_api_url + "lpl1", channel_LPL);
        channel_LMS.clear();
        channel_LMS.add("LMS");
        channel_LMS.add("lms1");
        send_request(twitch_api_url + "lms1", channel_LMS);
        channel_RIOT.clear();
        channel_RIOT.add("RiotGames");
        channel_RIOT.add("riotgames");
        send_request(twitch_api_url + "riotgames", channel_RIOT);
        mSwipeLayout.setRefreshing(false);
    }
    public void parseJSON(String mResponse, ArrayList<String> mArr){
        String mName = new String();
        try {
            JSONObject mjObject = new JSONObject(mResponse);
            mName = mjObject.getJSONObject("stream").getJSONObject("channel").getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mName == null || mName.isEmpty()){
            mName = mArr.get(0).toString() + ": Offline";
        }
        mArr.add(mName);
        mLoLStreamsArr.add(mArr);

        ArrayList<LoLStream> arrayOfStreams = LoLStream.getLoLStreams(mLoLStreamsArr);
        LoLStreamsArrayAdapter adapter = new LoLStreamsArrayAdapter(faActivity, arrayOfStreams);
        mlistView.setAdapter(adapter);
    }
    public void send_request(String var_url, final ArrayList<String> mArr) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseJSON(response, mArr);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(200, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        faActivity  = (FragmentActivity) super.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_lol_streams, container, false);
        queue = Volley.newRequestQueue(faActivity);
        mlistView = (ListView) rootView.findViewById(R.id.listView);
        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.relativelayout);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                drawList();
            }
        });

        drawList();

        return rootView;
    }

}
