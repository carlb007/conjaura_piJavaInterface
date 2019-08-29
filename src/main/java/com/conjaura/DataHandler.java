package com.conjaura;

public class DataHandler {

    private static byte[] headerData = new byte[]{0,0,0,0,0};
    public static Segment segments;
    public static IO dataIO;

    public DataHandler(){
        segments = new Segment();
        segments.createSegments();
        segments.createSegmentData();
        dataIO = new IO();
        transferColourData();
        transferGammaData();
        transferPanelConfig();
        prepForStreaming();
    }


    private static void prepForStreaming(){
        /*START OF INIT*/
        dataIO.pingMCU();
        buildAndTxHeader("data","");
        dataIO.haltTilReady();
        //SEND DATA SEGMENT LENGTHS
        dataIO.spiTransfer(segments.getSegmentLengths());
        dataIO.haltTilReady();
        try{
            Thread.sleep(100);
        }catch(InterruptedException e){
            System.out.println("Wait interupted "+e.getMessage());
        }
        System.out.println("Segment setup complete");
    }

    private static void transferPanelConfig(){
        /*START OF PANEL CONFIG*/
        dataIO.pingMCU();
        buildAndTxHeader("config","panelSetup");
        dataIO.haltTilReady();
        //SEND CONFIG DATA
        buildAndTxConfig();
        dataIO.haltTilReady();
        System.out.println("Config setup complete");
    }

    private static void transferColourData(){
        /*START OF COLOUR MODE*/
        dataIO.pingMCU();
        buildAndTxHeader("config","colourSetup");
        dataIO.haltTilReady();
        if(ColourConf.getColourMode() == ColourModes.PALETTE_COLOUR){
            dataIO.spiTransfer(ColourConf.getPaletteData());
            dataIO.haltTilReady();
        }
        System.out.println("Colour setup complete");
    }

    public static void transferGammaData(){
        /*START OF GAMMA MODE*/
        dataIO.pingMCU();
        buildAndTxHeader("config","gammaSetup");
        dataIO.haltTilReady();
        dataIO.spiTransfer(ColourConf.getGammaData());
        dataIO.haltTilReady();
        System.out.println("Gamma setup complete");
    }

    public static void buildAndTxConfig(){
        byte[] configData = new byte[4*ConjauraSetup.getPanelCount()];
        int pos=0;
        for(int i=0;i<ConjauraSetup.getPanelCount();i++){
            Panel thisPanel = Panel.getPanel(i);
            if (thisPanel.width % 8 == 0 && thisPanel.height % 8 == 0 && thisPanel.width<=32 && thisPanel.height<=32) {

                byte byte1 = 0;
                byte byte2 = 0;
                byte byte3 = 0;
                byte byte4 = 0;

                //BYTE 1:

                byte bits8_7 = 0;
                byte bits6_5 = 0;
                byte bits4_3 = 0;
                byte bit2 = 0;
                byte bit1 = 0;

                bits8_7 = (byte)((((int)thisPanel.width / 8)-1) << 6);
                bits6_5 = (byte)((((int)thisPanel.height / 8)-1) << 4);
                bits4_3 = (byte)(thisPanel.orientation.ordinal() << 2);
                bit2 = (byte)(thisPanel.scanLines.ordinal() << 1);

                byte1 = (byte)(bits8_7 | bits6_5 | bits4_3 | bit2 | bit1);

                //System.out.println( "Byte 1:"+byte1+", "+bits8_7+", "+bits6_5+", "+bits4_3+", "+bit2+" "+thisPanel.width);

                // BYTE 2:
                byte bit8= 0;
                byte bits7_6 = 0;
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
                byte bits8_6 = 0;
                byte bits5_4 = 0;
                byte bits3_2 = 0;
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


    public static void buildAndTxHeader(String mode,String submode) {
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
            hBits2_2 = (byte) Segment.totalSegments;
        }
        else if (mode.equals("address")){
            hBits1_1 = 64;
            if(submode.equals("request")){
                hBits2_1 = 0;
            }
            else if(submode.equals("reset")){
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
                byte3 = (byte) ConjauraSetup.getPanelCount();
                int configDataLen = ConjauraSetup.getPanelCount() * 4;
                hBits2_4 = (byte) (configDataLen >> 8 & 63);
                byte5 = (byte) (configDataLen & 255);
            }
            else if (submode.equals("colourSetup")) {
                hBits2_1 = 16;
                byte hBits3_1 = 0;
                byte hBits4_1 = 0;
                if (ColourConf.getColourMode() == ColourModes.TRUE_COLOUR) {
                    hBits3_1 = 0;
                } else if (ColourConf.getColourMode() == ColourModes.HIGH_COLOUR) {
                    hBits3_1 = 4;
                    hBits4_1 = (byte) ColourConf.getHcBiasMode().ordinal();
                } else if (ColourConf.getColourMode() == ColourModes.PALETTE_COLOUR) {
                    hBits3_1 = 8;
                }



                hBits3and4_1 = (byte) (hBits3_1 | hBits4_1);

                hBits2_2 = (byte) (ConjauraSetup.getBamBits().ordinal());
                byte3 = (byte) (ColourConf.getPaletteSize());

                if (ColourConf.getColourMode() == ColourModes.PALETTE_COLOUR) {
                    int paletteLen = (ColourConf.getPaletteSize() + 1) * 3;
                    hBits2_4 = (byte) (paletteLen >> 8 & 63);
                    byte5 = (byte) (paletteLen & 255);
                }
            }
            else if (submode.equals("gammaSetup")) {
                hBits2_1 = 32;
                hBits2_4 = (byte) (ColourConf.getGammaSize() >> 8 & 63);
                byte5 = (byte) (ColourConf.getGammaSize() & 255);
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
    }


}
