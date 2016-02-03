package com.example.remotecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
    private ArrayList<ArrayList<String>> mTorrentsArray = new ArrayList<>();
    private SharedPreferences mSettings;
    private String myUrl;
    private Handler mHandler = new Handler();
    private boolean mRetry;

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
                ArrayList<String> mTorrentsPrefs = new ArrayList<>();
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("name"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("percentDone"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("rateDownload"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("rateUpload"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("status"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("id"));
                mTorrentsPrefs.add(mjArray.getJSONObject(i).getString("eta"));
                mTorrentsArray.add(mTorrentsPrefs);
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
            myArguments.put("metainfo", Base64.encodeToString(torrentFile, Base64.DEFAULT));
//            myArguments.put("filename", "http://ru-tor.net/download/458581");
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-add");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request(myUrl, myReqParams, false);
    }
    public void startTorrent(int id_) {
        JSONObject myReqParams = new JSONObject();
        JSONObject myArguments = new JSONObject();
        try {
            myArguments.put("ids", id_);
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-start");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request(myUrl, myReqParams, false);
    }
    public void stopTorrent(int id_) {
        JSONObject myReqParams = new JSONObject();
        JSONObject myArguments = new JSONObject();
        try {
            myArguments.put("ids", id_);
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-stop");
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
        myFields.put("percentDone");
        myFields.put("rateDownload");
        myFields.put("rateUpload");
        myFields.put("status");
        myFields.put("id");
        myFields.put("eta");
        try {
            myArguments.put("fields", myFields);
            myReqParams.put("arguments",myArguments);
            myReqParams.put("method", "torrent-get");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request(myUrl, myReqParams, true);
    }
    public int myProgress(int i) {
        double dProgress = Double.valueOf(mTorrentsArray.get(i).get(1))*1000;
        int iProgress = (int)(dProgress);
        return iProgress;
    }
    public String myParsedSpeed(String mySpeed) {
        double dSpeed = Double.valueOf(mySpeed)/1000000;
        mySpeed = String.format("%.2f", dSpeed);
        return mySpeed;
    }
    public String myETA(int i) {
        final String eta_;
        double eta_in_seconds_ = Double.valueOf(mTorrentsArray.get(i).get(6));
        double dHours = eta_in_seconds_ / 3600;
        int iHours = (int)(dHours);
        double dMinutes = (eta_in_seconds_ - iHours*3600)/60;
        int iMinutes = (int)(dMinutes);
        double dSeconds = eta_in_seconds_ - iHours*3600 - iMinutes*60;
        int iSeconds = (int)(dSeconds);
        if (eta_in_seconds_ < 0) {
            eta_ = "";
        } else if (iHours > 24 || eta_in_seconds_ < 0) {
            eta_ = ">1d";
        } else if (iHours > 0) {
            eta_ = iHours + "h " + iMinutes + "m";
        } else {
            eta_ = iMinutes + "m " + iSeconds + "s";
        }
        return eta_;
    }
    public void myDrawTable() {
        myTable.removeAllViews();
        int myWidth = myTable.getWidth();
        for (int i = 0; i < mTorrentsArray.size(); i++ ) {

//            PlaylistFragment.myTextView3.setText(mTorrentsArray.toString());
            final int id_ = Integer.valueOf(mTorrentsArray.get(i).get(5));

            TableRow row1 = new TableRow(getActivity());
            row1.setLayoutParams(new TableLayout.LayoutParams(myWidth, dpToPx(80)));
            row1.setId(1000 + i);
            myTable.addView(row1);

            RelativeLayout myRLayout = new RelativeLayout(getActivity());
            myRLayout.setLayoutParams(new TableRow.LayoutParams(myWidth, TableRow.LayoutParams.MATCH_PARENT));
            row1.addView(myRLayout);

            TextView myTextView1 = new TextView(getActivity());
            myTextView1.setText(mTorrentsArray.get(i).get(0));
            myTextView1.setLayoutParams(new RelativeLayout.LayoutParams(myWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
            myRLayout.addView(myTextView1);
            RelativeLayout.LayoutParams rLParams = (RelativeLayout.LayoutParams)myTextView1.getLayoutParams();
//            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            rLParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(80), 0);
            myTextView1.setMaxLines(1);
            myTextView1.setEllipsize(TextUtils.TruncateAt.END);
            myTextView1.setTypeface(Typeface.DEFAULT_BOLD);

            TextView myTextView2 = new TextView(getActivity());
            myTextView2.setText("download: " + myParsedSpeed(mTorrentsArray.get(i).get(2)) + " Mb/s   upload: " + myParsedSpeed(mTorrentsArray.get(i).get(3)) + " Mb/s");
            myTextView2.setLayoutParams(new RelativeLayout.LayoutParams(myWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
            myRLayout.addView(myTextView2);
            RelativeLayout.LayoutParams sLParams = (RelativeLayout.LayoutParams)myTextView2.getLayoutParams();
//            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            sLParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            sLParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            sLParams.setMargins(dpToPx(10), 0, dpToPx(80), dpToPx(10));
            myTextView2.setMaxLines(1);

            ProgressBar myProgressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
            myProgressBar.setLayoutParams(new RelativeLayout.LayoutParams(dpToPx(70), dpToPx(70)));
            myProgressBar.setId(5000 + i);
            myRLayout.addView(myProgressBar);
            RelativeLayout.LayoutParams pbParams = (RelativeLayout.LayoutParams)myProgressBar.getLayoutParams();
            pbParams.addRule(RelativeLayout.CENTER_VERTICAL);
            pbParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            pbParams.setMargins(0, 0, dpToPx(5), 0);

            myProgressBar.setProgressDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_progress_bar));
            myProgressBar.setIndeterminate(false);
            if (Double.valueOf(mTorrentsArray.get(i).get(1)) < 1 && mTorrentsArray.get(i).get(4).equals("0")) {
                myProgressBar.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.download_circle_shape));
                myProgressBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startTorrent(id_);
                    }
                });
            } else if (Double.valueOf(mTorrentsArray.get(i).get(1)) < 1 && mTorrentsArray.get(i).get(4).equals("4")) {
                myProgressBar.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.stop_circle_shape));
                myProgressBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopTorrent(id_);
                    }
                });
                TextView myTextView3 = new TextView(getActivity());
                myTextView3.setText(myETA(i));
                myTextView3.setLayoutParams(new RelativeLayout.LayoutParams(dpToPx(70), dpToPx(70)));
                myRLayout.addView(myTextView3);
                RelativeLayout.LayoutParams tParams = (RelativeLayout.LayoutParams)myTextView3.getLayoutParams();
                tParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                tParams.addRule(RelativeLayout.CENTER_VERTICAL);
                tParams.setMargins(0, 0, dpToPx(5), 0);
                myTextView3.setMaxLines(1);
                myTextView3.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myTextView3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                myTextView3.setTextColor(Color.parseColor("#FFFFFF"));
            } else if (mTorrentsArray.get(i).get(4).equals("6")){
                myProgressBar.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.complete_circle_shape));
                stopTorrent(id_);
            } else {
                myProgressBar.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.complete_circle_shape));
            }
            myProgressBar.setMax(1001);
            myProgressBar.setProgress(myProgress(i)+1);
//            PlaylistFragment.myTextView3.setText("progress: " + myProgressBar.getProgress() + "\nmyProgress: " + myProgress(i));

            View mySeparator = new View(getActivity());
            mySeparator.setLayoutParams(new TableRow.LayoutParams(myWidth, dpToPx(1)));
            mySeparator.setBackgroundColor(Color.argb(255, 195, 195, 195));
            mySeparator.setId(1000 + i);

            myTable.addView(mySeparator);
        }
        View myWSpace = new View(getActivity());
        myWSpace.setLayoutParams(new TableRow.LayoutParams(myWidth, dpToPx(60)));
        myTable.addView(myWSpace);
    }

    public void send_request(String var_url, final JSONObject myReqParams, final Boolean... parse) {
        JsonObjectRequest myJsonRequest = new JsonObjectRequest(Request.Method.POST, var_url, myReqParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (parse[0]) {
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

        listTorrents();
        updateTorrentsList();

        return rootView;
    }

}