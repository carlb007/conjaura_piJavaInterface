package com.conjaura;


class Segment {
    static final int MAX_SEG_SIZE = 2048;

    private byte startPanelID;
    private byte endPanelID;
    private int segmentDataSize;
    byte[] dataStream;

    Segment(byte start, byte end, int segSize){
        startPanelID = start;
        endPanelID = (byte)(end-1);
        segmentDataSize = segSize;
        dataStream = new byte[segSize];
    }

    int getSegmentLength(){
        return segmentDataSize;
    }
    int getStartID(){ return startPanelID; }
    int getEndID(){
        return endPanelID;
    }
}
