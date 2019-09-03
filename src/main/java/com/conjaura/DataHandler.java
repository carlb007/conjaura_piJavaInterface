package com.conjaura;

import java.util.ArrayList;

class DataHandler {
    private ArrayList<Segment> dataSegments;
    ArrayList<Panel> panels;
    private ConjauraSetup parentConfig;
    private IO dataIO;
    private int allSegmentsSize;

    DataHandler(ConjauraSetup setup){
        parentConfig = setup;
        dataSegments = new ArrayList<>();
        panels = new ArrayList<>();
        dataIO = new IO();
    }

    int transferFrame(){
        createSegmentData();
        for(Segment thisSegment : dataSegments){
            dataIO.spiTransfer(thisSegment.dataStream);
            dataIO.haltTilReady();
        }
        return allSegmentsSize;
    }

    void createSegments(){
        byte lastPanel = 0;
        byte startPanel = 0;
        while(lastPanel<panels.size()) {
            int segmentSize = 0;
            byte start = lastPanel;
            for(Panel thisPanel : panels.subList(start,panels.size())){
                if ((segmentSize + thisPanel.dataLength)<Segment.MAX_SEG_SIZE){
                    lastPanel++;
                    segmentSize += thisPanel.dataLength;
                }
                else{
                    break;
                }
            }
            dataSegments.add(new Segment(startPanel, lastPanel, segmentSize));
            allSegmentsSize += segmentSize;
            startPanel = lastPanel;
        }
    }

    private void createSegmentData(){
        for(Segment thisSegment : dataSegments){
            for(Panel thisPanel : panels.subList(thisSegment.getStartID(),thisSegment.getEndID())){
                System.arraycopy(thisPanel.ledData, 0, thisSegment.dataStream,
                            0, thisPanel.ledData.length);
                if(thisPanel.edgeActive){
                    System.arraycopy(thisPanel.edgeData, 0, thisSegment.dataStream,
                            thisPanel.ledData.length, thisPanel.edgeData.length);
                }
            }
        }
    }

    private byte[] getSegmentLengths() {
        byte[] segmentData = new byte[dataSegments.size() * 2];
        int pos = 0;
        for(Segment thisSegment : dataSegments){
            segmentData[pos++] = (byte) ((thisSegment.getSegmentLength() >> 8) & 255);
            segmentData[pos++] = (byte) (thisSegment.getSegmentLength() & 255);
        }
        return segmentData;
    }


    void initDisplay(){
        System.out.println("Init display");
        IO.resetMCU();
        dataIO.haltTilReady();
        IO.setLed("off");
        transferColourData();
        transferGammaData();
        transferPanelConfig();
        prepForStreaming();
        try {
            Thread.sleep(100);
        }
        catch(InterruptedException e){
            System.out.println( e.getMessage());
        }
    }


    private void prepForStreaming(){
        /*START OF INIT*/
        dataIO.pingMCU();
        buildAndTxHeader("data","");
        dataIO.haltTilReady();
        //SEND DATA SEGMENT LENGTHS
        dataIO.spiTransfer(getSegmentLengths());
        dataIO.haltTilReady();
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
        System.out.println("Segment setup complete");
    }

    private void transferPanelConfig(){
        /*START OF PANEL CONFIG*/
        dataIO.pingMCU();
        buildAndTxHeader("config","panelSetup");
        dataIO.haltTilReady();
        //SEND CONFIG DATA
        buildAndTxConfig();
        dataIO.haltTilReady();
        System.out.println("Config setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
    }

    private void transferColourData(){
        /*START OF COLOUR MODE*/
        dataIO.pingMCU();
        buildAndTxHeader("config","colourSetup");
        dataIO.haltTilReady();
        if(parentConfig.colourSetup.getColourMode() == ColourModes.PALETTE_COLOUR){
            dataIO.spiTransfer(parentConfig.colourSetup.getPaletteData());
            dataIO.haltTilReady();
        }
        System.out.println("Colour setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
    }

    private void transferGammaData(){
        /*START OF GAMMA MODE*/
        dataIO.pingMCU();
        buildAndTxHeader("config","gammaSetup");
        dataIO.haltTilReady();
        dataIO.spiTransfer(parentConfig.colourSetup.getGammaData());
        dataIO.haltTilReady();
        System.out.println("Gamma setup complete");
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
    }

    private void buildAndTxConfig(){
        byte[] configData = new byte[4*panels.size()];
        int pos=0;
        for(Panel thisPanel : panels){
            if (thisPanel.width % 8 == 0 && thisPanel.height % 8 == 0 && thisPanel.width<=32 && thisPanel.height<=32) {

                byte byte1;
                byte byte2;
                byte byte3;
                byte byte4;

                //BYTE 1:

                byte bits8_7;
                byte bits6_5;
                byte bits4_3;
                byte bit2;
                byte bit1 = 0;

                bits8_7 = (byte)((((int)thisPanel.width / 8)-1) << 6);
                bits6_5 = (byte)((((int)thisPanel.height / 8)-1) << 4);
                bits4_3 = (byte)(thisPanel.orientation.ordinal() << 2);
                bit2 = (byte)(thisPanel.scanLines.ordinal() << 1);

                byte1 = (byte)(bits8_7 | bits6_5 | bits4_3 | bit2 | bit1);

                // BYTE 2:
                byte bit8= 0;
                byte bits7_6;
                byte bits5_1 = 0;

                if(thisPanel.ledActive){
                    bit8 = (byte)(1 << 7);
                }
                bits7_6 = (byte)(thisPanel.throttle.ordinal() << 5);

                byte2 = (byte)(bit8 | bits7_6 | bits5_1);

                //BYTE 3:
                bit8 = 0;
                bits7_6 = 0;
                byte bit5 = 0;
                byte bit4 = 0;
                byte bit3 = 0;
                byte bits2_1 = 0;

                if(thisPanel.touchEnabled) {
                    bit8 = (byte) (1 << 7);
                    bits7_6 = (byte)(thisPanel.touchChannelsFlag << 5);
                    bit5 = (byte)(thisPanel.touchDataSize.ordinal() << 4);
                }

                if(thisPanel.edgeActive) {
                    bit4 = (byte)(1 << 3);
                    bit3= (byte)(thisPanel.edgeThrottle.ordinal() << 2);
                    bits2_1 = (byte)(thisPanel.edgeDensity.ordinal());
                }

                byte3 = (byte)(bit8 | bits7_6 | bit5 | bit4 | bit3 | bits2_1);

                //BYTE4
                byte bits8_6;
                byte bits5_4;
                byte bits3_2;
                bit1 = 0;

                bits8_6 = (byte)(thisPanel.peripheralType.ordinal() << 5);
                bits5_4 = (byte)(thisPanel.peripheralSettings << 3);
                bits3_2 = (byte)(thisPanel.peripheralReturnSize << 1);

                byte4 = (byte)(bits8_6 | bits5_4 | bits3_2 | bit1);

                configData[pos++]=byte1;
                configData[pos++]=byte2;
                configData[pos++]=byte3;
                configData[pos++]=byte4;

            }
            else{
                throw new IllegalArgumentException("Invalid panel size");
            }
        }
        dataIO.spiTransfer(configData);
    }


    private void buildAndTxHeader(String mode,String submode) {
        byte[] headerData = new byte[]{0,0,0,0,0};
        byte hBits1_1 = 0;
        byte hBits2_1 = 0;
        byte hBits3and4_1 = 0;
        byte hBits1_2 = 0;
        byte hBits2_2 = 0;
        byte byte3 = 0;
        byte hBits1_4 = 0;
        byte hBits2_4 = 0;
        byte byte5 = 0;

        if (mode.equals("data")) {
            hBits2_2 = (byte) dataSegments.size();
        }
        else if (mode.equals("address")){
            hBits1_1 = 64; //DEFAULT FOR REQUEST MODE
            if(submode.equals("reset")){
                hBits2_1 = 16;
            }
            else if(submode.equals("finish")){
                hBits2_1 = 32;
            }
        }
        else if(mode.equals("config")) {
            hBits1_1 = (byte) 128;
            if (submode.equals("panelSetup")) {
                hBits2_1 = 0;
                byte3 = (byte) panels.size();
                int configDataLen = panels.size() * 4;
                hBits2_4 = (byte) (configDataLen >> 8 & 63);
                byte5 = (byte) (configDataLen & 255);
            }
            else if (submode.equals("colourSetup")) {
                hBits2_1 = 16;
                byte hBits3_1 = 0; //DEFAULT FOR TRUE COLOUR
                byte hBits4_1 = 0;
                if (parentConfig.colourSetup.getColourMode() == ColourModes.HIGH_COLOUR) {
                    hBits3_1 = 4;
                    hBits4_1 = (byte) parentConfig.colourSetup.getHcBiasMode().ordinal();
                } else if (parentConfig.colourSetup.getColourMode() == ColourModes.PALETTE_COLOUR) {
                    hBits3_1 = 8;
                }



                hBits3and4_1 = (byte) (hBits3_1 | hBits4_1);

                hBits2_2 = (byte) (parentConfig.getBamBits().ordinal());
                byte3 = (byte) (parentConfig.colourSetup.getPaletteSize());

                if (parentConfig.colourSetup.getColourMode() == ColourModes.PALETTE_COLOUR) {
                    int paletteLen = (parentConfig.colourSetup.getPaletteSize() + 1) * 3;
                    hBits2_4 = (byte) (paletteLen >> 8 & 63);
                    byte5 = (byte) (paletteLen & 255);
                }
            }
            else if (submode.equals("gammaSetup")) {
                hBits2_1 = 32;
                hBits2_4 = (byte) (parentConfig.colourSetup.getGammaSize() >> 8 & 63);
                byte5 = (byte) (parentConfig.colourSetup.getGammaSize() & 255);
            }
            else {
                throw new IllegalArgumentException("Invalid primary config mode");
            }
        }
        byte byte1 = (byte)(hBits1_1 | hBits2_1 | hBits3and4_1);
        byte byte2 = (byte)(hBits1_2 | hBits2_2);
        byte byte4 = (byte)(hBits1_4 | hBits2_4);
        headerData[0] = byte1;
        headerData[1] = byte2;
        headerData[2] = byte3;
        headerData[3] = byte4;
        headerData[4] = byte5;

        dataIO.spiTransfer(headerData);
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
        System.out.println("Header Sent");
    }


}
