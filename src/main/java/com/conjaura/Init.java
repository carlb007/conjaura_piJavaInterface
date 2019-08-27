package com.conjaura;

import java.util.ArrayList;
import java.util.Collections;

class Init{
    public static Init instance;

    private static ConjauraSetup config;
    private static ColourConf colourSetup;
    private static Segment segments;
    private static IO dataIO;
    public static DataHandler dataHandler;

    public static void main( String[] args )
    {
        instance = new Init();
    }

    Init(){
        dataIO = new IO();
        config = new ConjauraSetup(16);
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }
        config.setColourMode(ColourModes.TRUE_COLOUR);
        config.setBam(BamBitSize.BAM_8BIT);
        config.setScan(ScanLines.SCAN_LINES8);

        for(int i=0;i< config.panelCount;i++){
            Panel thisPanel = new Panel((byte)16,(byte)16);
            thisPanel.enableLeds();
            if(i==0){
                thisPanel.setTouch(true,(byte)16,TouchSensitivity.DATA_8BIT);
            }
            thisPanel.setEdge(true, EdgeLedThrottle.NONE, EdgeLedDensity.THREE_PER_EIGHT);
        }
        //System.out.println("TC "+config.panels[0].dataLength);
        colourSetup = new ColourConf();
        colourSetup.dummyGamma();
        segments = new Segment();
        segments.createSegments();
        segments.createSegmentData();
        dataHandler.buildConfig();

        dataIO.setPanelPower("on");
        dataIO.setLed("blue");
        dataIO.resetMCU();
        dataIO.haltTilReady();
        dataIO.pingMCU();
        System.out.println("MCU Ready");
        try{
            Thread.sleep(1000);
        }catch(InterruptedException e){

        }

        /*START OF COLOUR MODE*/
        dataIO.pingMCU();
        //ArrayList<Byte> headerData;// = new ArrayList<Byte>(5);
        byte[] headerData = dataHandler.buildHeader("config","colourSetup");
        //System.out.println("Header: "+headerData[0]+" "+headerData[1]+" "+headerData[2]+" "+headerData[3]+" "+headerData[4]);
        dataIO.spiTransfer(headerData);
        dataIO.haltTilReady();
        dataIO.pingMCU();
        if(config.colourMode == ColourModes.PALETTE_COLOUR){

        }
        System.out.println("Colour setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){

        }


        /*START OF GAMMA MODE*/
        dataIO.pingMCU();
        headerData = dataHandler.buildHeader("config","gammaSetup");
        dataIO.spiTransfer(headerData);
        dataIO.haltTilReady();
        dataIO.pingMCU();
        //SEND GAMMA
        dataIO.spiTransfer(colourSetup.gammaData);
        dataIO.haltTilReady();
        dataIO.pingMCU();
        System.out.println("Gamma setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){

        }

        /*START OF PANEL CONFIG*/
        dataIO.pingMCU();
        headerData = dataHandler.buildHeader("config","panelSetup");
        dataIO.spiTransfer(headerData);
        dataIO.haltTilReady();
        dataIO.pingMCU();
        //SEND CONFIG DATA
        dataIO.spiTransfer(dataHandler.getConfigData());
        dataIO.haltTilReady();
        dataIO.pingMCU();
        System.out.println("Config setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){

        }

        /*START OF INIT*/
        dataIO.pingMCU();
        headerData = dataHandler.buildHeader("data","");
        dataIO.spiTransfer(headerData);
        dataIO.haltTilReady();
        dataIO.pingMCU();
        //SEND DATA SEGMENT LENGTHS
        dataIO.spiTransfer(segments.getSegmentLengths());
        dataIO.haltTilReady();
        dataIO.pingMCU();
        System.out.println("Segment setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){

        }

        /*START OF STREAM*/
        int loopsToRun = 100000;
        int rStart = 0;
        int gStart = 767;
        int loopsForSpeed = 200;
        long start = System.currentTimeMillis();
        int tallyBytes = 0;

        for(int loops=0;loops<loopsToRun;loops++){
            ArrayList<Byte>panelData = new ArrayList<Byte>((Collections.nCopies(768,(byte)0)));
            for(int r=0;r<256;r++){
                panelData.set(rStart,(byte)(r/16));
                rStart+=3;
                if(rStart>765){
                    rStart=0;
                }
            }

            rStart+=3;
            if(rStart>765){
                rStart=0;
            }

            for(int x=0;x<config.panelCount;x++){
                Panel.setData(panelData,x);
            }
            segments.createSegmentData();
            for(int segment=0;segment<segments.totalSegments;segment++){
                dataIO.spiTransfer(segments.getSegmentData(segment));
                tallyBytes += segments.getSegmentLength(segment);
                loopsForSpeed--;
                if(loopsForSpeed==0){
                    long end = System.currentTimeMillis();
                    float timeLen = (end-start) / 1000F;
                    int bytesPerSecond = (int)(tallyBytes/timeLen);
                    System.out.println(tallyBytes+" Bytes in "+timeLen+"seconds ("+bytesPerSecond+" per second)");
                    loopsForSpeed = 200;
                    tallyBytes = 0;
                    start = System.currentTimeMillis();
                }
                dataIO.haltTilReady();
                dataIO.pingMCU();
            }
        }


    }

    public static ConjauraSetup getGlobalConfig(){
        return config;
    }

}
