package com.conjaura;

import java.util.ArrayList;
import java.util.Iterator;

public class Segment {
    static final int MAX_SEG_SIZE = 2048;
    public static int totalSegments = 0;
    public static ArrayList<Segment> dataSegments = new ArrayList<Segment>();
    private byte startPanelID;
    private byte endPanelID;
    private int segmentDataSize;
    private ArrayList<Byte> dataStream;


    public Segment(){
        //NOTHING TO DO ON OBJ INIT
    }

    public Segment(byte start, byte end, int segSize){
        this.startPanelID = start;
        this.endPanelID = (byte)(end-1);
        this.segmentDataSize = segSize;
        this.dataStream = new ArrayList<Byte>(segSize);

        System.out.println("SEGSIZE "+this.dataStream.size()+" "+segSize);
        dataSegments.add(this);
    }

    public static int getSegmentLength(int id){
        return dataSegments.get(id).segmentDataSize;
    }

    public static byte[] getSegmentData(int id){
        int segLength = getSegmentLength(id);
        byte[] returnData = new byte[segLength];
        ArrayList<Byte> Data = dataSegments.get(id).dataStream;
        for (int i = 0; i < segLength; i++){
            returnData[i] = Data.get(i).byteValue();
        }
        return returnData;
    }

    public static byte[] getSegmentLengths(){
        byte[] segmentData = new byte[totalSegments*2];
        int pos=0;
        for(int i=0;i<totalSegments;i++){
            segmentData[pos++] = (byte)((getSegmentLength(i)>>8) & 255);
            segmentData[pos++] = (byte)(getSegmentLength(i) & 255);
        }
        return segmentData;
    }

    public static void createSegments(){
        byte lastPanel = 0;
        byte startPanel = 0;
        while(lastPanel<ConjauraSetup.getPanelCount()) {
            int segmentSize = 0;
            byte start = lastPanel;
            for (byte i = start; i < ConjauraSetup.getPanelCount(); i++) {
                Panel thisPanel = Panel.getPanel(i);
                if ((segmentSize + thisPanel.dataLength)<MAX_SEG_SIZE){
                    lastPanel++;
                    segmentSize += thisPanel.dataLength;
                }
                else{
                    break;
                }
            }
            new Segment(startPanel, lastPanel, segmentSize);
            totalSegments++;
            startPanel = lastPanel;
        }
    }


    public static void createSegmentData(){
        for(int x=0;x<totalSegments;x++) {
            Segment thisSegment = dataSegments.get(x);
            thisSegment.dataStream.clear();
            for (byte i = thisSegment.startPanelID; i < thisSegment.endPanelID + 1; i++) {
                Panel thisPanel = Panel.getPanel(i);
                thisSegment.dataStream.addAll(thisPanel.ledData);
                thisSegment.dataStream.addAll(thisPanel.edgeData);
            }
        }
    }
}
