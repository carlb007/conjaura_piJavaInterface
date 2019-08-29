package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

public class TestStream {

    public TestStream(){
        /*START OF STREAM*/
        int loopsToRun = 100000;
        int rStart = 0;
        int gStart = 767;
        int loopsForSpeed = 1000;
        long start = System.currentTimeMillis();
        int tallyBytes = 0;

        for(int loops=0;loops<loopsToRun;loops++){
            ArrayList<Byte> panelData = new ArrayList<Byte>((Collections.nCopies(768,(byte)0)));
            for(int r=0;r<256;r++){
                panelData.set(rStart,(byte)(r));
                rStart+=3;
                if(rStart>765){
                    rStart=0;
                }
            }

            rStart+=3;
            if(rStart>765){
                rStart=0;
            }

            for(int x=0;x<ConjauraSetup.getPanelCount();x++){
                Panel.setData(panelData,x);
            }
            DataHandler.segments.createSegmentData();
            for(int segment=0;segment<DataHandler.segments.dataSegments.size();segment++){
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
        }
    }
}
