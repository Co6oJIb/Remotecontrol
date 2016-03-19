package com.example.remotecontrol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Дмитрий on 08.02.2016.
 */
public class addPLitem {
    public String name;
    public String path;
    public String type;

    public addPLitem(String name, String path, String type) {
        this.name = name;
        this.path = path;
        this.type = type;
    }

    public static ArrayList<addPLitem> get_addPLitems(ArrayList<ArrayList<String>> m_addPLArray) {
        ArrayList<addPLitem> addPLitems = new ArrayList<addPLitem>();
        String mName = "";
        for (int i=0; i<m_addPLArray.size(); i++ ) {
            try {
                mName = new String(m_addPLArray.get(i).get(0).getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();
            }
            addPLitems.add(new addPLitem(mName, m_addPLArray.get(i).get(1), m_addPLArray.get(i).get(2)));
        }
        return addPLitems;
    }
}
