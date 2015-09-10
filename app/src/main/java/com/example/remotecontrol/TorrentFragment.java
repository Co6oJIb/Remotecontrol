package com.example.remotecontrol;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TorrentFragment extends Fragment {
    private FragmentActivity faActivity;
    private TableLayout myTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_torrent, container, false);
        faActivity  = (FragmentActivity)    super.getActivity();

        myTable = (TableLayout) rootView.findViewById(R.id.tableLayout);

        for (int i = 0; i < 100; i++ ) {
            TextView myTextView1 = new TextView(getActivity());
            myTextView1.setText("Text " + i);
            TableRow row1 = new TableRow(getActivity());
            row1.addView(myTextView1);
            View mySeparator = new View(getActivity());
            mySeparator.setBackgroundColor(0xFFC3C3C3);
            myTable.addView(row1);
            myTable.addView(mySeparator);
        }

        return rootView;
    }

}