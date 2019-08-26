package com.conjaura;

public class ColourConf {
    public static byte[] paletteData;
    public static byte[] gammaData;
    ConjauraSetup config = Init.getGlobalConfig();

    public ColourConf(){}

    public void dummyGamma(){
        int gamLength = 0;
        if(config.colourMode == ColourModes.TRUE_COLOUR){
            System.out.println("TC "+config.colourMode);
            gamLength = 256*3;
        }
        else if(config.colourMode == ColourModes.HIGH_COLOUR){
            if(config.hcBias == HighColourBias.EVEN){
                gamLength = 32*3;
            }
            else{
                gamLength = 32+32+64;
            }
        }
        else if(config.colourMode == ColourModes.PALETTE_COLOUR){
            gamLength = 256*3;
        }
        gammaData = new byte[gamLength];
        config.gammaSize = (short)gamLength;
        System.out.println( "Gamma Size:"+gamLength);
        for(int i=0;i<gamLength;i++){
            gammaData[i] = 16;
        }
    }

    public void dummyPalette(){
        if(config.paletteSize>0){
            paletteData = new byte[config.paletteSize];
            for(int i=0;i<((config.paletteSize+1)*3);i++) {
                paletteData[i] = 8;
            }
            System.out.println( "Dummy Palette Length:"+((config.paletteSize+1)*3) );
            System.out.println( "Palette Size:"+(config.paletteSize+1) );
        }
    }

    public void setGamma(byte[] data){

    }

    public void setPalette(byte[] data){

    }
}
