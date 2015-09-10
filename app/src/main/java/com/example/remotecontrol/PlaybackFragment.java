package com.example.remotecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaybackFragment extends Fragment implements View.OnClickListener {
    private FragmentActivity    faActivity;


    private SeekBar mSeekBar;
    private SeekBar mVolumeBar;
    private Button myButton;
    private Button myButton2;
    private Button myButton3;
    private Button myButton4;
    private Button myButton5;
    private Button myButton6;
    private Button myButton7;
    private Button myButton8;
    private Button myButton9;
    private Button myButton10;
    private TextView mTextView;
    private TextView mTextView2;

    private Handler mHandler = new Handler();
    private RequestQueue queue;
    private List<String> mSubArray = new ArrayList<>();
    private List<String> mAudioArray = new ArrayList<>();
    private SharedPreferences mSettings;


    private String url;
    private String mBytes;
    private int icp = 0;
    private int imp = 100;
    private int mMaxVolume = 256;
    private int mCurVolume;
    private int mSubTrack = 0;
    private int mAudioTrack = 0;
    private boolean stopUpdateBar = false;
    private boolean stopUpdateVolumeBar = false;
    private double mSubDelay = 0;
    private double mAudioDelay = 0;

    public String convertToTime(int mSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("Russia/Moscow"));
        return formatter.format(new Date(mSeconds));
    }

    private Runnable mUpdateTimeTask = new Runnable() {

        @Override
        public void run() {
            url="http://" + mSettings.getString("address1", "") + ":8080/requests/status.json";
            if (!stopUpdateBar) {
                send_request(url);
                int mCurrentPosition = icp;
                int mMaxPosition = imp;
                mSeekBar.setMax(mMaxPosition);
                mSeekBar.setProgress(mCurrentPosition);
            }
            if (!stopUpdateVolumeBar) {
                mVolumeBar.setMax(mMaxVolume);
                mVolumeBar.setProgress(mCurVolume);
            }
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
            mAudioArray.clear();
            mSubArray.add("0");
            for (int i=0; i<mjArray.length(); i++ ) {
                Pattern pattern = Pattern.compile("^Stream.*$");
                Matcher matcher = pattern.matcher(mjArray.get(i).toString());
                if (matcher.matches()) {
                    if (mjObject3.getJSONObject(mjArray.get(i).toString()).getString("Type").matches("Subtitle")) {
                        mSubArray.add(mjArray.get(i).toString().replaceAll("Stream ", ""));
                    }
                    else if (mjObject3.getJSONObject(mjArray.get(i).toString()).getString("Type").matches("Audio")) {
                        mAudioArray.add(mjArray.get(i).toString().replaceAll("Stream ", ""));
                    }
                }
            }
            String mp = mjObject1.getString("length");
            String cp = mjObject1.getString("time");
            set_imp(getInt(mp));
            set_icp(getInt(cp));
            if (!stopUpdateVolumeBar) {
                mCurVolume = getInt(mjObject1.getString("volume"));
            }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (mAudioArray != null && mAudioArray.size()>0) {
                    if (mAudioTrack + 1 < mAudioArray.size()) {
                        mAudioTrack++;
                    } else {
                        mAudioTrack = 0;
                    }
                    send_request(url + "?command=audio_track&val=" + mAudioArray.get(mAudioTrack));
                }
                break;
            case R.id.button2:
                send_request(url + "?command=pl_pause");
                break;
            case R.id.button3:
                Intent intent = new Intent(faActivity, mPrefsFragment.class);
                startActivity(intent);
                break;
            case R.id.button4:
                if (mSubArray != null && mSubArray.size()>0) {
                    if (mSubTrack + 1 < mSubArray.size()) {
                        mSubTrack++;
                    } else {
                        mSubTrack = 0;
                    }
                    send_request(url + "?command=subtitle_track&val=" + mSubArray.get(mSubTrack));
                }
                break;
            case R.id.button5:
                send_request(url + "?command=pl_previous");
                break;
            case R.id.button6:
                send_request(url + "?command=pl_next");
                break;
            case R.id.button7:
                mSubDelay = mSubDelay + 0.5;
                send_request(url + "?command=subdelay&val=" + mSubDelay);
                break;
            case R.id.button8:
                mSubDelay = mSubDelay - 0.5;
                send_request(url + "?command=subdelay&val=" + mSubDelay);
                break;
            case R.id.button9:
                mAudioDelay = mAudioDelay - 0.5;
                send_request(url + "?command=audiodelay&val=" + mAudioDelay);
                break;
            case R.id.button10:
                mAudioDelay = mAudioDelay + 0.5;
                send_request(url + "?command=audiodelay&val=" + mAudioDelay);
                break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        faActivity  = (FragmentActivity)    super.getActivity();
        View rootView = inflater.inflate(R.layout.fragment_playback, container, false);


        myButton = (Button) rootView.findViewById(R.id.button);
        myButton2 = (Button) rootView.findViewById(R.id.button2);
        myButton3 = (Button) rootView.findViewById(R.id.button3);
        myButton4 = (Button) rootView.findViewById(R.id.button4);
        myButton5 = (Button) rootView.findViewById(R.id.button5);
        myButton6 = (Button) rootView.findViewById(R.id.button6);
        myButton7 = (Button) rootView.findViewById(R.id.button7);
        myButton8 = (Button) rootView.findViewById(R.id.button8);
        myButton9 = (Button) rootView.findViewById(R.id.button9);
        myButton10 = (Button) rootView.findViewById(R.id.button10);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mVolumeBar = (SeekBar) rootView.findViewById(R.id.seekBar2);
        mTextView = (TextView) rootView.findViewById(R.id.textView);
        mTextView2 = (TextView) rootView.findViewById(R.id.textView2);

        queue = Volley.newRequestQueue(faActivity);

        myButton.setOnClickListener(this);
        myButton2.setOnClickListener(this);
        myButton3.setOnClickListener(this);
        myButton4.setOnClickListener(this);
        myButton5.setOnClickListener(this);
        myButton6.setOnClickListener(this);
        myButton7.setOnClickListener(this);
        myButton8.setOnClickListener(this);
        myButton9.setOnClickListener(this);
        myButton10.setOnClickListener(this);

        mSettings = PreferenceManager.getDefaultSharedPreferences(faActivity);
        updateProgressBar();

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                icp = progress;
                mTextView.setText(convertToTime(icp * 1000) + " / " + convertToTime(imp * 1000));
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
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurVolume = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                stopUpdateVolumeBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                send_request(url + "?command=volume&val=" + mCurVolume);
                stopUpdateVolumeBar = false;
            }
        });
        return rootView;
    }
}
