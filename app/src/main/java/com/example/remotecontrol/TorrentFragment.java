package com.example.remotecontrol;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
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

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

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
            row1.setId(1000 + i);
            RelativeLayout myRLayout = new RelativeLayout(getActivity());
            myRLayout.addView(myTextView1);
            RelativeLayout.LayoutParams rLParams = (RelativeLayout.LayoutParams)myTextView1.getLayoutParams();
            rLParams.addRule(RelativeLayout.CENTER_VERTICAL);
            myTextView1.setLayoutParams(rLParams);
            myRLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, dpToPx(40)));
            row1.addView(myRLayout);
            View mySeparator = new View(getActivity());
            mySeparator.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, dpToPx(1)));
            mySeparator.setBackgroundColor(Color.argb(255, 195, 195, 195));
            mySeparator.setId(1000 + i);

            myTable.addView(row1);
            myTable.addView(mySeparator);
        }

        return rootView;
    }

}