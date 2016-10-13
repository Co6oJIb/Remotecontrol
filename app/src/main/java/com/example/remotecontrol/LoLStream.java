package com.example.remotecontrol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Дмитрий on 04.02.2016.
 */
public class LoLStream {
    public String name;
    public String channel;
    public String cName;

    public LoLStream(String name, String channel, String cName) {
        this.name = name;
        this.channel = channel;
        this.cName = cName;
    }

    public static ArrayList<LoLStream> getLoLStreams(ArrayList<ArrayList<String>> mLoLStreamsArr) {
        ArrayList<LoLStream> LoLStreams = new ArrayList<LoLStream>();
        String mName = "";
        for (int i=0; i<mLoLStreamsArr.size(); i++ ) {
            mName = new String(mLoLStreamsArr.get(i).get(0));
            LoLStreams.add(new LoLStream(mName, mLoLStreamsArr.get(i).get(1), mLoLStreamsArr.get(i).get(2)));
        }
        return LoLStreams;
    }
}
