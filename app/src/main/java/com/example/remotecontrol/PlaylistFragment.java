package com.example.remotecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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


    private ArrayList<ArrayList<String>> mPLArray = new ArrayList<>();
    private ArrayList<ArrayList<String>> m_addPLArray = new ArrayList<>();
    private ArrayList<ArrayList<String>> m_items_to_add = new ArrayList<>();
    private ArrayList<String> mArr = new ArrayList();
    private FragmentActivity faActivity;
    private ListView mlistView;
    private ListView mlistView2;
    private ImageButton mAddButton;
    private ImageButton mDelButton;
    private LinearLayout mLinearLayout;
    private Button mCancelButton;
    private Button mEnqueueButton;
    private Button mPlayButton;
    private TextView mTextView4;
    private ProgressBar mProgressBar;
    private RequestQueue queue;
    private SharedPreferences mSettings;
    private String mBytes;
    private Handler mHandler = new Handler();

    private Runnable mUpdateTextView = new Runnable() {

        @Override
        public void run() {
            mTextView4.setText(Integer.toString(PlaybackFragment.mIcp));
            mHandler.postDelayed(this, 250);
        }
    };
    public void updateTextView() {
        mHandler.postDelayed(mUpdateTextView, 100);
    }

    private class PLaddArrayAdapter extends ArrayAdapter<addPLitem> {
        public PLaddArrayAdapter(Context context, ArrayList<addPLitem> addPLitems) {
            super(context, 0, addPLitems);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final addPLitem plitem = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pl_text_view, parent, false);
            }
            // Lookup view for data population
            final TextView addPLitemName = (TextView) convertView.findViewById(R.id.textView3);
            // Populate the data into the template view using the data object
            addPLitemName.setText(plitem.name);
            final String url = "http://192.168.0.39:8080/requests/browse.json?dir=" + plitem.path.replace("file://","");
            addPLitemName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send_request(url, "add_files");
                }
            });
            addPLitemName.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ArrayList<String> item_prefs = new ArrayList();
                    item_prefs.add(plitem.path);
                    item_prefs.add(plitem.type);
                    m_items_to_add.add(item_prefs);
                    addPLitemName.setBackgroundColor(Color.parseColor("#CDCDCD"));
                    return true;
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }
    public void mParseJSON_add_dir(String mResponse) {
        try {
            JSONObject mjObject1 = new JSONObject(mResponse);
            JSONArray mjArray = mjObject1.getJSONArray("element");
            for (int i=0; i<mjArray.length(); i++ ) {
                if (mjArray.getJSONObject(i).getString("type").matches("dir") && !mjArray.getJSONObject(i).getString("name").matches("^..$")) {
                    String url = "http://192.168.0.39:8080/requests/browse.json?dir=" + mjArray.getJSONObject(i).getString("uri").replace("file://","");
                    send_request(url, "add_dir");
                }
                if (mjArray.getJSONObject(i).getString("type").matches("file")) {
                    String url = "http://192.168.0.39:8080/requests/status.json?command=in_enqueue&input=" + mjArray.getJSONObject(i).getString("uri");
                    send_request(url, "playlist");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        send_request("http://192.168.0.39:8080/requests/playlist.json", "playlist");
    }
    public void mParseJSON_add_files(String mResponse) {
        try {
            JSONObject mjObject1 = new JSONObject(mResponse);
            JSONArray mjArray = mjObject1.getJSONArray("element");
            m_addPLArray.clear();
            for (int i=0; i<mjArray.length(); i++ ) {
                ArrayList<String> m_addPLItem = new ArrayList<>();
                m_addPLItem.add(mjArray.getJSONObject(i).getString("name"));
                m_addPLItem.add(mjArray.getJSONObject(i).getString("uri"));
                m_addPLItem.add(mjArray.getJSONObject(i).getString("type"));
                m_addPLArray.add(m_addPLItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<addPLitem> arrayOfPLitems = addPLitem.get_addPLitems(m_addPLArray);
        PLaddArrayAdapter adapter = new PLaddArrayAdapter(faActivity, arrayOfPLitems);
        mlistView2.setAdapter(adapter);
    }

    public void mParseJSON_playlist(String mResponse) {
        try {

            JSONObject mjObject1 = new JSONObject(mResponse);
            JSONArray mjArray = mjObject1.getJSONArray("children");
            mPLArray.clear();
            for (int i=0; i<mjArray.length(); i++ ) {
                if (mjArray.getJSONObject(i).getString("name").matches("Playlist")) {
                    for (int k=0; k<mjArray.getJSONObject(i).getJSONArray("children").length(); k++ ) {
                        ArrayList<String> mPLItem = new ArrayList<>();
                        mPLItem.add(mjArray.getJSONObject(i).getJSONArray("children").getJSONObject(k).getString("name"));
                        mPLItem.add(mjArray.getJSONObject(i).getJSONArray("children").getJSONObject(k).getString("id"));
                        mPLArray.add(mPLItem);
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<PLitem> arrayOfPLitems = PLitem.getPLitems(mPLArray);
        PLArrayAdapter adapter = new PLArrayAdapter(faActivity, arrayOfPLitems);
        mlistView.setAdapter(adapter);
    }

    public void send_request(String var_url,final String parse) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (parse.equals("playlist")) {
                            mParseJSON_playlist(response);
                        }
                        if (parse.equals("add_files")) {
                            mParseJSON_add_files(response);
                        }
                        if (parse.equals("add_dir")) {
                            mParseJSON_add_dir(response);
                        }
                        if (parse.equals("clear")) {
                            send_request("http://192.168.0.39:8080/requests/playlist.json", "playlist");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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

    public void send_custom_request(String var_url,final String parse, final ArrayList<String> mtArr) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (parse.equals("playlist")) {
//                            mParseJSON_playlist(response);
                        }
                        if (parse.equals("add_files")) {
//                            mParseJSON_add_files(response);
                        }
                        if (parse.equals("add_dir")) {
//                            mParseJSON_add_dir(response);
                        }
                        send_request("http://192.168.0.39:8080/requests/playlist.json", "playlist");
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(faActivity, "Items added to playlist", Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(faActivity, "Fault", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                mBytes = "co6ojib:" + mSettings.getString("password1", "");
                try {
                    params.put("Authorization", "Basic " + Base64.encodeToString(mBytes.getBytes("UTF-8"), Base64.DEFAULT));
                } catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                return params;
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                for (int i=0; i<mArr.size(); i++ ) {
                    String key = "number_array[" + i + "]";
                    params.put(key, mArr.get(i));
                }
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        faActivity  = (FragmentActivity) super.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        queue = Volley.newRequestQueue(faActivity);
        mlistView = (ListView) rootView.findViewById(R.id.listView);
        mlistView2 = (ListView) rootView.findViewById(R.id.listView2);
        mTextView4 = (TextView) rootView.findViewById(R.id.textView4);
        mLinearLayout = (LinearLayout) rootView.findViewById(R.id.linearlayout);

        //updateTextView();

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mCancelButton = (Button) rootView.findViewById(R.id.button12);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlistView2.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mEnqueueButton.setVisibility(View.GONE);
                mPlayButton.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.GONE);
                send_request("http://192.168.0.39:8080/requests/playlist.json", "playlist");
            }
        });
        mEnqueueButton = (Button) rootView.findViewById(R.id.button13);
        mEnqueueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArr.clear();
                for (int i=0; i<m_items_to_add.size(); i++ ){
                    if (m_items_to_add.get(i).get(1).equals("dir")) {
//                        String url = "http://192.168.0.39:8080/requests/browse.json?dir=" + m_items_to_add.get(i).get(0).replace("file://","");
//                        send_request(url, "add_dir");
                        mArr.add(m_items_to_add.get(i).get(0).replace("file://",""));
                    } else {
                        String url = "http://192.168.0.39:8080/requests/status.json?command=in_enqueue&input=" + m_items_to_add.get(i).get(0);
                        send_request(url, "playlist");
                    }
                }
//                mArr.add("9090909090");
                String url = "http://192.168.0.39/test.php";
                send_custom_request(url, "add_dir", mArr);

                m_items_to_add.clear();
                mlistView2.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mEnqueueButton.setVisibility(View.GONE);
                mPlayButton.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.GONE);
            }
        });
        mPlayButton = (Button) rootView.findViewById(R.id.button14);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mAddButton = (ImageButton) rootView.findViewById(R.id.button11);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request("http://192.168.0.39:8080/requests/browse.json?dir=/home/co6ojib/media", "add_files");
                mlistView2.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.VISIBLE);
                mEnqueueButton.setVisibility(View.VISIBLE);
                mPlayButton.setVisibility(View.VISIBLE);
                mLinearLayout.setVisibility(View.VISIBLE);
            }
        });
        mDelButton = (ImageButton) rootView.findViewById(R.id.imageButton);
        mDelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_request("http://192.168.0.39:8080/requests/status.json?command=pl_empty", "clear");
            }
        });
        send_request("http://192.168.0.39:8080/requests/playlist.json", "playlist");
        send_request("http://192.168.0.39:8080/requests/browse.json?dir=/home/co6ojib/media", "add_files");

        return rootView;

    }

}
