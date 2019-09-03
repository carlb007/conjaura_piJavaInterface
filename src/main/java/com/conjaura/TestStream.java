package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

public class TestStream {
    public TestStream(){}

    public void run(){
        /*START OF STREAM*/
        try {
            Thread.sleep(500);
        }
        catch(InterruptedException e){

        }
        Init.config.dataHandler.initDisplay();
        try {
            Thread.sleep(100);
        }
        catch(InterruptedException e){

        }
        System.out.println( "Stream start");
        int loopsToRun = 2000;
        int rStart = 0;
        int gStart = 767;
        int loopsForSpeed = 1000;
        long start = System.currentTimeMillis();
        int tallyBytes = 0;
        int panelCount = ConjauraSetup.getPanelCount();
        int segmentCount = Init.config.dataHandler.segments.dataSegments.size();
        System.out.println("segs: "+segmentCount);
        byte[] panelData = new byte[768];//ArrayList<Byte>((Collections.nCopies(768,(byte)1)));
        for(int loops=0;loops<loopsToRun;loops++){

            for(int r=0;r<256;r++){
                //panelData.set(rStart,(byte)(r));
                panelData[rStart] = (byte)r;
                rStart+=3;
                if(rStart>765){
                    rStart=0;
                }
            }

            rStart+=3;
            if(rStart>765){
                rStart=0;
            }

            for(int x=0;x<panelCount;x++){
                Panel.setData(panelData,x);
            }
            DataHandler.segments.createSegmentData();
            for(int segment=0;segment<segmentCount;segment++){
                //System.out.println("seg len: "+DataHandler.segments.getSegmentData(segment).length);
                DataHandler.dataIO.spiTransfer(DataHandler.segments.getSegmentData(segment));
                tallyBytes += DataHandler.segments.getSegmentLength(segment);
                loopsForSpeed--;
                if(loopsForSpeed==0){
                    long end = System.currentTimeMillis();
                    float timeLen = (end-start) / 1000F;
                    int bytesPerSecond = (int)(tallyBytes/timeLen);
                    System.out.println(tallyBytes+" Bytes in "+timeLen+"seconds ("+(bytesPerSecond/1000)+"KB per second)");
                    loopsForSpeed = 1000;
                    tallyBytes = 0;
                    start = System.currentTimeMillis();
                }
                DataHandler.dataIO.haltTilReady();
            }
            //System.out.println("done loop");
        }
    }
}
