package com.example.remotecontrol;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                return new PlaybackFragment();
            case 1:
                return new PlaylistFragment();
            case 2:
                return new TorrentFragment();
            case 3:
                return new LoL_StreamsFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 4;
    }
    @Override
    public CharSequence getPageTitle(int index)
    {
        String title = "Playback";
        if (index == 0) title = "Playback";
        if (index == 1) title = "Playlist";
        if (index == 2) title = "Torrent";
        if (index == 3) title = "LoL Streams";
        return title;
    }
}