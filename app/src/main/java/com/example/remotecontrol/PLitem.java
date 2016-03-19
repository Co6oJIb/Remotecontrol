package com.example.remotecontrol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Дмитрий on 04.02.2016.
 */
public class PLitem {
    public String name;
    public String id;

    public PLitem(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public static ArrayList<PLitem> getPLitems(ArrayList<ArrayList<String>> mPLArray) {
        ArrayList<PLitem> PLitems = new ArrayList<PLitem>();
        String mName = "";
        for (int i=0; i<mPLArray.size(); i++ ) {
            try {
                mName = new String(mPLArray.get(i).get(0).getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
            }
            PLitems.add(new PLitem(mName, mPLArray.get(i).get(1)));
        }
        return PLitems;
    }
}
