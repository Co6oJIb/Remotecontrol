package com.example.remotecontrol;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

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

public class FullscreenActivity extends Activity implements View.OnClickListener {

    private SeekBar mSeekBar;
    private Button myButton;
    private Button myButton2;
    private Button myButton3;
    private Button myButton4;
    private Button myButton5;
    private Button myButton6;
    private TextView mTextView;
    private EditText mEditText;
    private EditText mEditText2;
    private Handler mHandler = new Handler();
    private RequestQueue queue;
    private List<String> mSubArray = new ArrayList<>();

//    private String url="http://" + mEditText.getText() + ":8080/requests/status.json";
    private String url="http://192.168.0.65:8080/requests/status.json";
    private String mBytes;
    private String mAddress;
    private String mPassword;
    private int icp = 0;
    private int imp = 100;
    private int mSubTrack = 0;
    private boolean stopUpdateBar = false;

    public String convertToTime(int mSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("Russia/Moscow"));
        return formatter.format(new Date(mSeconds));
    }

    private Runnable mUpdateTimeTask = new Runnable() {

        @Override
        public void run() {
            if (!stopUpdateBar) {
                send_request(url);
                int mCurrentPosition = icp;
                int mMaxPosition = imp;
                mSeekBar.setMax(mMaxPosition);
                mSeekBar.setProgress(mCurrentPosition);
            }
            url="http://" + mAddress + ":8080/requests/status.json";
            mHandler.postDelayed(this, 250);
        }
    };
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public void set_imp(int var_imp) {
        imp = var_imp;
    }
    public void set_icp(int var_icp) {
        icp = var_icp;
    }
    public int getInt(String s){
        return Integer.parseInt(s.replaceAll("[\\D]", ""));
    }
    public static void enableStrictMode(Context context) {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()
                        .penaltyLog()
                        .build());
        StrictMode.setVmPolicy(
                new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .penaltyLog()
                        .build());
    }
    public void mParseJSON(String mResponse) {
        try {
            JSONObject mjObject1 = new JSONObject(mResponse);
            if (mjObject1.getString("state").matches("playing")){
                myButton2.setBackgroundResource(android.R.drawable.ic_media_pause);
            }
            else if (mjObject1.getString("state").matches("paused")){
                myButton2.setBackgroundResource(android.R.drawable.ic_media_play);
            }
            else {
                myButton2.setBackgroundResource(android.R.drawable.ic_media_play);
                mTextView.setText("--:--:--" + " / " + "--:--:--");
                icp = 0;
                mSeekBar.setProgress(0);
            }
            JSONObject mjObject2 = mjObject1.getJSONObject("information");
            JSONObject mjObject3 = mjObject2.getJSONObject("category");
            JSONArray mjArray = mjObject3.names();
            mSubArray.clear();
            mSubArray.add("0");
            for (int i=0; i<mjArray.length(); i++ ){
                Pattern pattern = Pattern.compile("^Stream.*$");
                Matcher matcher = pattern.matcher(mjArray.get(i).toString());
                if (matcher.matches()) {
                    if (mjObject3.getJSONObject(mjArray.get(i).toString()).getString("Type").matches("Subtitle")) {
                        mSubArray.add(mjArray.get(i).toString().replaceAll("Stream ", ""));
                    }
                }
            }
            String mp = mjObject1.getString("length");
            String cp = mjObject1.getString("time");
            set_imp(getInt(mp));
            set_icp(getInt(cp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void send_request(String var_url) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, var_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mParseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                myButton2.setBackgroundResource(android.R.drawable.ic_media_play);
                mTextView.setText("--:--:--" + " / " + "--:--:--");
//                mTextView.setText(mBytes);
//                stopUpdateBar = true;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                mBytes = ":" + mPassword;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mAddress = mEditText.getText().toString();
                mPassword = mEditText2.getText().toString();
                break;
            case R.id.button2:
                send_request(url + "?command=pl_pause");
                break;
            case R.id.button3:
                send_request(url + "?command=pl_stop");
                break;
            case R.id.button4:
                if (mSubTrack + 1 < mSubArray.size()){
                    mSubTrack++;
                }
                else {
                    mSubTrack = 0;
                }
                send_request(url + "?command=subtitle_track&val=" + mSubArray.get(mSubTrack));
                break;
            case R.id.button5:
                send_request(url + "?command=pl_previous");
                break;
            case R.id.button6:
                send_request(url + "?command=pl_next");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableStrictMode(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fullscreen);

        myButton = (Button) findViewById(R.id.button);
        myButton2 = (Button) findViewById(R.id.button2);
        myButton3 = (Button) findViewById(R.id.button3);
        myButton4 = (Button) findViewById(R.id.button4);
        myButton5 = (Button) findViewById(R.id.button5);
        myButton6 = (Button) findViewById(R.id.button6);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mTextView = (TextView) findViewById(R.id.textView);
        mEditText = (EditText) findViewById(R.id.editText);
        mEditText2 = (EditText) findViewById(R.id.editText2);
        queue = Volley.newRequestQueue(this);

        myButton.setOnClickListener(this);
        myButton2.setOnClickListener(this);
//        myButton3.setOnClickListener(this);
        myButton4.setOnClickListener(this);
        myButton5.setOnClickListener(this);
        myButton6.setOnClickListener(this);

        updateProgressBar();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                icp = progress;
                mTextView.setText(convertToTime(icp*1000) + " / " + convertToTime(imp * 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdateBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                send_request(url + "?command=seek&val=" + icp);
                stopUpdateBar = false;
            }
        });
    }
}
