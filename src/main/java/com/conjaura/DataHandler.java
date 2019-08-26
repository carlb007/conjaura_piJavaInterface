package com.conjaura;

import java.util.ArrayList;

public class DataHandler {

    private static ArrayList<Integer> validWidthsHeights = new ArrayList<Integer>();
    private static ArrayList<Byte> headerData = new ArrayList<Byte>(5);
    private static ArrayList<Byte> configData;


    static ConjauraSetup config = Init.getGlobalConfig();

    public DataHandler(){
        validWidthsHeights.add(8);
        validWidthsHeights.add(16);
        validWidthsHeights.add(24);
        validWidthsHeights.add(32);
    }

    public static ArrayList<Byte> getConfigData(){
        return configData;
    }

    public static void buildConfig(){
        configData = new ArrayList<Byte>(4*config.panelCount);
        for(int i=0;i<config.panelCount;i++){
            Panel thisPanel = Panel.getPanel(i);
            if (validWidthsHeights.contains(thisPanel.width) && validWidthsHeights.contains(thisPanel.height)) {

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
                bit2 = (byte)(config.scanLines.ordinal() << 1);

                byte1 = (byte)(bits8_7 | bits6_5 | bits4_3 | bit2 | bit1);

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

                configData.add(byte1);
                configData.add(byte2);
                configData.add(byte3);
                configData.add(byte4);

            }
            else{
                //ERROR
            }

        }
    }


    public static ArrayList<Byte> buildHeader(String[] mode,String[] submode) {
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
                byte3 = (byte) config.panelCount;
                int configDataLen = config.panelCount * 4;
                hBits2_4 = (byte) (configDataLen >> 8 & 63);
                byte5 = (byte) (configDataLen & 255);
            }
            else if (submode.equals("colourSetup")) {
                hBits2_1 = 16;
                byte hBits3_1 = 0;
                if (config.colourMode == ColourModes.TRUE_COLOUR) {
                    hBits3_1 = 0;
                } else if (config.colourMode == ColourModes.HIGH_COLOUR) {
                    hBits3_1 = 4;
                } else if (config.colourMode == ColourModes.PALETTE_COLOUR) {
                    hBits3_1 = 8;
                }

                byte hBits4_1 = (byte) config.hcBias.ordinal();

                hBits3and4_1 = (byte) (hBits3_1 | hBits4_1);

                hBits2_2 = (byte) (config.bamBits.ordinal());
                byte3 = (byte) (config.paletteSize);

                if (config.colourMode == ColourModes.PALETTE_COLOUR) {
                    int paletteLen = (config.paletteSize + 1) * 3;
                    hBits2_4 = (byte) (paletteLen >> 8 & 63);
                    byte5 = (byte) (paletteLen & 255);
                }
            }
            else if (submode.equals("gammaSetup")) {
                hBits2_1 = 32;
                hBits2_4 = (byte) (config.gammaSize >> 8 & 63);
                byte5 = (byte) (config.gammaSize & 255);
            }
            else {
                config.lastError = "Invalid primary config mode";
            }
        }
        byte byte1 = (byte)(hBits1_1 | hBits2_1 | hBits3and4_1);
        byte byte2 = (byte)(hBits1_2 | hBits2_2);
        byte byte4 = (byte)(hBits1_4 | hBits2_4);
        headerData.add(byte1);
        headerData.add(byte2);
        headerData.add(byte3);
        headerData.add(byte4);
        headerData.add(byte5);

        return headerData;
    }


}