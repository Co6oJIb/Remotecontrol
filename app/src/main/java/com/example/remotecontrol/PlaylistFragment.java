package com.example.remotecontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
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


//    public static TextView myTextView3;
    private List<String> mPLArray = new ArrayList<>();
//    private List<String> mFilesArray = new ArrayList<>();
    private JSONObject mSubJObject = new JSONObject();
    public static ProgressBar myProgressBar;
    private FragmentActivity    faActivity;
    private TableLayout myTable;
    private RequestQueue queue;
    private SharedPreferences mSettings;
    private String mBytes;

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public void drawPL(List mPL) {
        myTable.removeAllViews();
        int myWidth = myTable.getWidth();
        String mName = "";
        for (int i = 0; i < mPL.size(); i++ ) {
            try {
                mName = new String(mPL.get(i).toString().getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            TableRow row1 = new TableRow(getActivity());
            row1.setLayoutParams(new TableLayout.LayoutParams(myWidth, dpToPx(80)));
            row1.setId(9000 + i);
            myTable.addView(row1);

            RelativeLayout myRLayout = new RelativeLayout(getActivity());
            myRLayout.setLayoutParams(new TableRow.LayoutParams(myWidth, TableRow.LayoutParams.MATCH_PARENT));
            row1.addView(myRLayout);

            TextView myTextView1 = new TextView(getActivity());
            myTextView1.setText(mName);
            myTextView1.setLayoutParams(new RelativeLayout.LayoutParams(myWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
            myRLayout.addView(myTextView1);
            RelativeLayout.LayoutParams rLParams = (RelativeLayout.LayoutParams)myTextView1.getLayoutParams();
//            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rLParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            rLParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(80), 0);
            myTextView1.setMaxLines(1);
            myTextView1.setEllipsize(TextUtils.TruncateAt.END);
            myTextView1.setTypeface(Typeface.DEFAULT_BOLD);

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
//            myTextView3.setText(mDirs + mFiles);
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

//            myTextView3.setText(mName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        drawPL(mPLArray);
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
        myTable = (TableLayout) rootView.findViewById(R.id.tableLayout2);
//        myTextView3 = (TextView) rootView.findViewById(R.id.textView3);
//        myProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        send_request("http://192.168.0.39:8080/requests/playlist.json");

        return rootView;

    }

}
