package com.example.remotecontrol;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.Menu;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    private static byte[] myData;
    public static byte[] getMyData() {
        return myData;
    }
    // Tab titles
    private String[] tabs = { "Playback", "Playlist", "Torrnet" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }
    @Override
    protected void onNewIntent(Intent intent){
        String action = intent.getAction();
        if (intent.ACTION_VIEW.equals(action)){
            handleTorrentUri(intent);
        }
    }
    public void handleTorrentUri(Intent intent) {
        Uri torrentUri = intent.getData();
        if (torrentUri != null) {
            getActionBar().setSelectedNavigationItem(2);
            File myFile = new File(torrentUri.getPath());
            myData = new byte[(int) myFile.length()];
            try {
                new FileInputStream(myFile).read(myData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            TorrentFragment myFragment = (TorrentFragment) getSupportFragmentManager().findFragmentById(R.id.pager);
            myFragment.addTorrent(myData);
        }
    }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}
