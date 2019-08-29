package com.conjaura;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ColourConf {
    private static ColourModes colourMode;
    private static HighColourBias hcBias;
    private static int paletteSize;
    private static int gammaSize;
    private static byte[] paletteData;
    private static byte[] gammaData;

    public ColourConf(){}
    public static ColourModes getColourMode(){return colourMode;}
    public static HighColourBias getHcBiasMode(){return hcBias;}
    public static int getGammaSize(){return gammaSize;}
    public static int getPaletteSize(){return paletteSize;}
    public static byte[] getPaletteData(){return paletteData;}
    public static byte[] getGammaData(){return gammaData;}

    public static void setColourMode(ColourModes mode){colourMode = mode;}
    public static void setHcBias(HighColourBias bias){hcBias = bias;}


    public void setGamma(JSONObject jsonGamma){
        JSONArray jsonGamRArray = (JSONArray)jsonGamma.get("red");
        JSONArray jsonGamGArray = (JSONArray)jsonGamma.get("green");
        JSONArray jsonGamBArray = (JSONArray)jsonGamma.get("blue");
        boolean state;
        if(colourMode == ColourModes.HIGH_COLOUR){
            if(hcBias == HighColourBias.EVEN){
                if(jsonGamRArray.size()==32 && jsonGamGArray.size()==32 && jsonGamBArray.size()==32){
                    state=true;
                }
                else{
                    throw new IllegalArgumentException("Invalid Gamma Array Size For HC Even");
                }
            }
            else{
                if(jsonGamRArray.size()+jsonGamGArray.size()+jsonGamBArray.size()==128){
                    state=true;
                }
                else{
                    throw new IllegalArgumentException("Invalid Gamma Array Size For HC Biased");
                }
            }
        }
        else{
            if(jsonGamRArray.size()==256 && jsonGamGArray.size()==256 && jsonGamBArray.size()==256){
                state=true;
            }
            else{
                throw new IllegalArgumentException("Invalid Gamma Array Size");
            }
        }

        if(state==true){
            gammaData = new byte[768];
            for(int i=0;i<jsonGamRArray.size();i++){
                gammaData[i]= (byte) (long)jsonGamRArray.get(i);
            }
            for(int i=0;i<jsonGamGArray.size();i++){
                gammaData[i+jsonGamRArray.size()]= (byte) (long)jsonGamGArray.get(i);
            }
            for(int i=0;i<jsonGamBArray.size();i++){
                gammaData[i+(jsonGamRArray.size()+jsonGamBArray.size())]= (byte) (long)jsonGamBArray.get(i);
            }
            gammaSize = (jsonGamRArray.size()+jsonGamGArray.size()+jsonGamBArray.size());
        }
    }


    public void setPalette(JSONArray jsonPalArray){
        if(jsonPalArray.size() % 3 == 0) {
            if(jsonPalArray.size()<=768) {
                paletteData = new byte[jsonPalArray.size()];
                paletteSize = jsonPalArray.size();
                for (int i = 0; i < jsonPalArray.size(); i++) {
                    paletteData[i] = (byte) (long) jsonPalArray.get(i);
                    System.out.println(paletteData[i]);
                }
            }
            else{
                throw new IllegalArgumentException("Palette Array Size Too Large. MAX 256 COLOURS");
            }
        }
        else{
            throw new IllegalArgumentException("Invalid Palette Array Size");
        }
    }
}
