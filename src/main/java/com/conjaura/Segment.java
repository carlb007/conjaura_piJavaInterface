package com.conjaura;

import java.util.ArrayList;

public class Segment {
    static final int MAX_SEG_SIZE = 10240;

    public static int totalSegments = 0;
    public static ArrayList<Segment> dataSegments = new ArrayList<Segment>();

    private byte startPanelID;
    private byte endPanelID;
    private int segmentDataSize;
    private ArrayList<Byte> dataStream;

    static ConjauraSetup config = Init.getGlobalConfig();

    public Segment(){
        //NOTHING TO DO ON OBJ INIT
    }

    public Segment(byte start, byte end, int segSize){
        this.startPanelID = start;
        this.endPanelID = (byte)(end-1);
        this.segmentDataSize = segSize;
        dataStream = new ArrayList<Byte>(segSize);

        System.out.println("SEGSIZE "+dataStream.size());
        dataSegments.add(this);
    }

    public int getSegmentLength(int id){
        return dataSegments.get(id).segmentDataSize;
    }

    public ArrayList<Byte> getSegmentData(int id){
        return dataSegments.get(id).dataStream;
    }

    public static void createSegments(){
        byte lastPanel = 0;
        byte startPanel = 0;
        while(lastPanel<config.panelCount) {
            int segmentSize = 0;
            byte start = lastPanel;
            for (byte i = start; i < config.panelCount; i++) {
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
            }
        }
    }
}
